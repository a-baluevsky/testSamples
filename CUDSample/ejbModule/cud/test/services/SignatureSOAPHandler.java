package cud.test.services;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;

import javax.xml.crypto.XMLStructure;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.xml.security.transforms.Transforms;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SignatureSOAPHandler implements SOAPHandler<SOAPMessageContext> {

	// @Override
	public Set<QName> getHeaders() {
		return null;
	}

	// @Override
	public void close(MessageContext mc) {
	}

	// @Override
	public boolean handleFault(SOAPMessageContext mc) {
		return true;
	}

	// @Override
	public boolean handleMessage(SOAPMessageContext mc) {

		try {
			SOAPMessage sm = mc.getMessage();
			
		
			SOAPHeader header = sm.getSOAPHeader();
			SOAPBody body = sm.getSOAPBody();

			char[] signingKeyPass = MainServicesTest.signingKeyPass;
			String signingAlias = MainServicesTest.signingAlias;

			KeyStore ks = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);

			PrivateKey privateKey = (PrivateKey) ks.getKey(signingAlias,
					signingKeyPass);

			Certificate cert = ks.getCertificate(signingAlias);
			PublicKey publicKey = cert.getPublicKey();

			// запрос
			if (Boolean.TRUE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				org.apache.xml.security.Init.init();

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				body.addNamespaceDeclaration(
						"wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				body.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", "Body");

				header.addNamespaceDeclaration(
						"wsu",
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
				header.setAttributeNS(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"wsu:Id", "Header");

				Node SecuritySOAP = header.getFirstChild();

				
				// timestamp
				QName timestampQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"Timestamp", "wsu");
				QName CreatedQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
						"Created", "wsu");

				SOAPElement timestampSOAP = ((SOAPElement) SecuritySOAP)
						.addChildElement(timestampQN);
				SOAPElement CreatedSOAP = timestampSOAP
						.addChildElement(CreatedQN);
				CreatedSOAP.addTextNode(DatatypeFactory.newInstance()
						.newXMLGregorianCalendar(new GregorianCalendar()).toXMLFormat());

				List<Transform> transformList = new ArrayList<Transform>();
				Transform transform = fac.newTransform(Transform.ENVELOPED,
						(XMLStructure) null);
				Transform transformC14N = fac.newTransform(
						Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
						(XMLStructure) null);
				transformList.add(transform);
				transformList.add(transformC14N);

				Reference ref1 = fac
						.newReference(
								"#Header",
								fac.newDigestMethod(
										"http://www.w3.org/2001/04/xmldsig-more#gostr3411",
										null), transformList, null, null);
				Reference ref2 = fac.newReference("#Body", fac.newDigestMethod(
						"http://www.w3.org/2001/04/xmldsig-more#gostr3411",
						null), transformList, null, null);
				List<Reference> referenceList = new ArrayList<Reference>();

				referenceList.add(ref1);
				referenceList.add(ref2);

				SignedInfo si = fac
						.newSignedInfo(
								fac.newCanonicalizationMethod(
										CanonicalizationMethod.EXCLUSIVE,
										(C14NMethodParameterSpec) null),
								fac.newSignatureMethod(
										"http://www.w3.org/2001/04/xmldsig-more#gostr34102001-gostr3411",
										null), referenceList);

				KeyInfoFactory kif = fac.getKeyInfoFactory();

				KeyValue kv = kif.newKeyValue(publicKey);
				KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

				javax.xml.crypto.dsig.XMLSignature sig = fac.newXMLSignature(
						si, ki);

				// куда вставлять подпись
				DOMSignContext signContext = new DOMSignContext(privateKey,
						SecuritySOAP);

				signContext.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

				signContext
						.setIdAttributeNS(
								body,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");
				signContext
						.setIdAttributeNS(
								header,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");

				sig.sign(signContext);

				sm.writeTo(System.out);
				
			} else {
				// ответ

				Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

				XMLSignatureFactory fac = XMLSignatureFactory.getInstance(
						"DOM", xmlDSigProvider);

				Node securityNode = header.getFirstChild();

				if (securityNode == null) {
					throw new Exception(
							"This service requires <wsse:Security>, which is missing!!!");
				}

				NodeList securityNodeChilds = securityNode.getChildNodes();

				Node signatureNode1 = null;

				for (int i = 0; i < securityNodeChilds.getLength(); i++) {

					if (securityNodeChilds.item(i).getLocalName() != null
							&& securityNodeChilds.item(i).getLocalName()
									.equals("Signature")) {
						signatureNode1 = securityNodeChilds.item(i);
					}
				}

				// signature

				if (signatureNode1 == null) {
					throw new Exception(
							"This service requires <dsig:Signature>, which is missing!!!");
				}

				
				DOMValidateContext valContext1 = new DOMValidateContext(
						publicKey, signatureNode1);

				valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

				valContext1
						.setIdAttributeNS(
								header,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");
				valContext1
						.setIdAttributeNS(
								body,
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"Id");

				javax.xml.crypto.dsig.XMLSignature signature1 = fac
						.unmarshalXMLSignature(valContext1);

				boolean result1 = signature1.validate(valContext1);

				System.out.println("SignValidateSOAPHandler:validate:" + result1);

			}

		} catch (Exception e) {
			System.out
					.println("SignatureSOAPHandler:handleMessage:error:"
							+ e);
			throw new ProtocolException(e);
		}
		return true;
	}

}
