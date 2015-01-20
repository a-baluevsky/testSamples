package cud.test.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

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
import javax.xml.crypto.dsig.keyinfo.KeyName;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;







/*
import ru.CryptoPro.ssl.SSLSocketFactoryImpl;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.transport.http.HTTPConduit;
*/
import org.apache.xml.security.transforms.Transforms;



import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author bubnov
 * Клиент вызова STS
 *
 */
public class CopyOfSTSClient {

	static String wsdlLocationURI = MainServicesTest.stsWsdlLocationURI ;

	public static Document issueSystemToken() throws Exception{
		return issueToken("SYSTEM", null);
	}
	
    public static Document issueUserToken() throws Exception{
    	return issueToken("USER_LOGIN", null);
	}

    public static Document issueUserOnBehalfOfToken(String user_onbehalfof_token) throws Exception{
    	return issueToken("USER_ONBEHALFOF", user_onbehalfof_token);
	}
    
public static Document issueToken(String type, String user_onbehalfof_token) throws Exception{

		Document result = null;
		try {
        
			
			//включается для HTTPS
			System.setProperty("javax.net.ssl.trustStore",
					MainServicesTest.trustStorePath);
			System.setProperty("javax.net.ssl.trustStorePassword", 
					MainServicesTest.trustStorePass);
			
			
			String ServiceName = "CUDSTS";
			String PortName = "CUDSTSPort";

			char[] signingKeyPass = MainServicesTest.signingKeyPass;
			String signingAlias = MainServicesTest.signingAlias;

			KeyStore ks = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);

			PrivateKey privateKey = (PrivateKey) ks.getKey(signingAlias,
					signingKeyPass);

			Certificate cert = ks.getCertificate(signingAlias);
			PublicKey publicKey = cert.getPublicKey();

			QName service = new QName("http://sts.services.cud.iac.spb.ru/",
					ServiceName);
			QName portName = new QName("http://sts.services.cud.iac.spb.ru/",
					PortName);

			URL wsdlLocation = new URL(wsdlLocationURI);

			Service jaxwsService = Service.create(wsdlLocation, service);

			Dispatch dispatch = jaxwsService.createDispatch(portName,
					SOAPMessage.class, Service.Mode.MESSAGE);
/*
			// ------------------
			HTTPConduit httpConduit = (HTTPConduit) ((org.apache.cxf.jaxws.DispatchImpl) dispatch)
					.getClient().getConduit();
			TLSClientParameters tlsCP = new TLSClientParameters();
			final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
			tlsCP.setSSLSocketFactory(sslFact);
			tlsCP.setDisableCNCheck(true);
			httpConduit.setTlsClientParameters(tlsCP);
			// ---------------------
*/
			MessageFactory mf = MessageFactory
					.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);

			Document doc =  get_saml_assertion_from_xml();;
			DOMSource src = new DOMSource(doc);
			
			SOAPMessage sm = mf.createMessage();
			
			sm.getSOAPPart().setContent(src);
			
			
			
			SOAPPart soapPart = sm.getSOAPPart();
			SOAPHeader header = sm.getSOAPHeader();
			SOAPBody body = sm.getSOAPBody();

			SOAPElement SecuritySOAP = (SOAPElement) header.getFirstChild();
			
			/*
			soapPart.getEnvelope()
					.addNamespaceDeclaration(
							"wsse",
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			soapPart.getEnvelope().addNamespaceDeclaration("ds",
					"http://www.w3.org/2000/09/xmldsig#");

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

			String system = MainServicesTest.system_code;
			
			if ("USER_ONBEHALFOF".equals(type)) {
				system = MainServicesTest.onbehalfof_system_code;
			}
			
			String username = MainServicesTest.user_login;
			String password = MainServicesTest.user_password;
			
			String x509Cert = null;
		
			//Document doc = sm.getSOAPPart();

			QName SecurityQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"Security", "wsse");
			QName SystemTokenQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"UsernameToken", "wsse");
			QName UsernameTokenQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"UsernameToken", "wsse");
			QName UsernameCertQN = new QName(
					"http://www.w3.org/2000/09/xmldsig#", "X509Certificate",
					"ds");

			QName SystemQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"Username", "wsse");
			QName UsernameQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"Username", "wsse");
			QName PasswordQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
					"Password", "wsse");
			QName timestampQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
					"Timestamp", "wsu");
			QName CreatedQN = new QName(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
					"Created", "wsu");

			SOAPElement SecuritySOAP = header.addChildElement(SecurityQN);

			SecuritySOAP
					.addNamespaceDeclaration(
							"wsu",
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
			SecuritySOAP
					.setAttributeNS(
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"wsu:Id", "_id_sec");

			// аутентификация системы
			SOAPElement SystemTokenSOAP = SecuritySOAP
					.addChildElement(SystemTokenQN);
			SOAPElement SystemSOAP = SystemTokenSOAP.addChildElement(SystemQN);
			SystemSOAP
					.addNamespaceDeclaration(
							"wsse",
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			SystemTokenSOAP
					.setAttributeNS(
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"wsu:Id", "SystemToken_1");
			SystemSOAP.addTextNode(system);

			// timestamp
			SOAPElement timestampSOAP = SecuritySOAP
					.addChildElement(timestampQN);
			SOAPElement CreatedSOAP = timestampSOAP.addChildElement(CreatedQN);
			CreatedSOAP.addTextNode(DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar()).toXMLFormat());

			if ("USER_LOGIN".equals(type)) {

				// аутентификация пользователя по логин/пароль
				SOAPElement UsernameTokenSOAP = SecuritySOAP
						.addChildElement(UsernameTokenQN);

				UsernameTokenSOAP
						.setAttributeNS(
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"wsu:Id", "UsernameToken_1");

				SOAPElement UsernameSOAP = UsernameTokenSOAP
						.addChildElement(UsernameQN);
				SOAPElement PasswordSOAP = UsernameTokenSOAP
						.addChildElement(PasswordQN);

				UsernameSOAP
						.addNamespaceDeclaration(
								"wsse",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				PasswordSOAP
						.addNamespaceDeclaration(
								"wsse",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				PasswordSOAP
						.setAttribute(
								"Type",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText");
				UsernameSOAP.addTextNode(username);
				PasswordSOAP.addTextNode(password);
			} else if ("USER_CERT".equals(type)) {

				// аутентификация пользователя по серификату
				SOAPElement UsernameCertSOAP = SecuritySOAP
						.addChildElement(UsernameCertQN);
				UsernameCertSOAP
						.setAttributeNS(
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"wsu:Id", "x509CertToken_1");
				String user_cert_file = "/userEtoken.cer";
				InputStream user_inStream = new FileInputStream(user_cert_file);
				CertificateFactory user_cf = CertificateFactory
						.getInstance("X.509");
				X509Certificate user_cert = (X509Certificate) user_cf
						.generateCertificate(user_inStream);
				x509Cert = new String(
						org.apache.commons.codec.binary.Base64.encodeBase64(
								user_cert.getEncoded(), false), "UTF-8");
				UsernameCertSOAP.addTextNode(x509Cert);
			}

			Certificate cert_hok = cert;

			String hok_cert_st = null;
			// только при аутентификации системы
			if ("SYSTEM".equals(type)) {
				hok_cert_st = new String(
						org.apache.commons.codec.binary.Base64.encodeBase64(
								cert_hok.getEncoded(), false), "UTF-8");
			}

			
			Document ws_trust_req = SamlDoc.getWSTrustRequestToken(
					hok_cert_st, user_onbehalfof_token);

		
			body.addDocument(ws_trust_req);
*/
			org.apache.xml.security.Init.init();

			Provider xmlDSigProvider = new ru.CryptoPro.JCPxml.dsig.internal.dom.XMLDSigRI();

			XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM",
					xmlDSigProvider);

			List<Transform> transformList = new ArrayList<Transform>();
			Transform transform = fac.newTransform(Transform.ENVELOPED,
					(XMLStructure) null);
			Transform transformC14N = fac.newTransform(
					Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS,
					(XMLStructure) null);
			transformList.add(transform);
			transformList.add(transformC14N);

			Reference ref1 = fac.newReference("#Header", fac.newDigestMethod(
					"http://www.w3.org/2001/04/xmldsig-more#gostr3411", null),
					transformList, null, null);
			Reference ref2 = fac.newReference("#Body", fac.newDigestMethod(
					"http://www.w3.org/2001/04/xmldsig-more#gostr3411", null),
					transformList, null, null);
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
			//KeyName kv = kif.newKeyName(publicKey.toString());
			
			KeyInfo ki = kif.newKeyInfo(Collections.singletonList(kv));

			javax.xml.crypto.dsig.XMLSignature sig = fac
					.newXMLSignature(si, ki);

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

			// ----------------------------------------------------------------
			// проверка подписи

			Node signatureNode1 = SecuritySOAP.getLastChild();

			DOMValidateContext valContext1 = new DOMValidateContext(publicKey,
					signatureNode1);
			valContext1.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

			valContext1
					.setIdAttributeNS(
							body,
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"Id");
			valContext1
					.setIdAttributeNS(
							header,
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"Id");

			javax.xml.crypto.dsig.XMLSignature signature1 = fac
					.unmarshalXMLSignature(valContext1);

			boolean result1 = signature1.validate(valContext1);

			System.out.println("dispatch:sign_validate:" + result1);

			sm.writeTo(System.out);
			
			// ----------------------------------------------------------------

			SOAPMessage reply = (SOAPMessage) dispatch.invoke(sm);

			SOAPHeader soapHeader_reply = reply.getSOAPHeader();
			SOAPBody soapBody_reply = reply.getSOAPBody();

			NodeList signatureList_reply = soapHeader_reply
					.getElementsByTagNameNS("*", "Signature");

			if (signatureList_reply == null
					|| signatureList_reply.getLength() == 0) {
				throw new Exception(
						"This service requires <dsig:Signature>, which is missing!!!");
			}

			PublicKey publicKey_reply = null;

			// X509Certificate
			NodeList x509CertificateList_reply = ((Element) signatureList_reply
					.item(0)).getElementsByTagNameNS("*", "X509Certificate");

			if (x509CertificateList_reply != null
					&& x509CertificateList_reply.getLength() > 0) {

				String x509Cert_Value = x509CertificateList_reply.item(0)
						.getTextContent();

				byte[] byteX509Certificate = 
						org.apache.commons.codec.binary.Base64.decodeBase64(x509Cert_Value.getBytes("UTF-8"));
			

				X509Certificate cert_reply = null;

				CertificateFactory cf = CertificateFactory.getInstance("X.509");
				ByteArrayInputStream bais = new ByteArrayInputStream(
						byteX509Certificate);

				while (bais.available() > 0)
					cert_reply = (X509Certificate) cf.generateCertificate(bais);

				if (cert_reply != null) {
					publicKey_reply = cert_reply.getPublicKey();
				}

			}

			// 2-я попытка - GOSTKeyValue
			if (publicKey_reply == null) {

				NodeList GOSTKeyList_reply = ((Element) signatureList_reply
						.item(0)).getElementsByTagNameNS("*", "GOSTKeyValue");

				if (GOSTKeyList_reply == null
						|| GOSTKeyList_reply.getLength() == 0) {
					System.out
							.println("TestServerCryptoSOAPHandler:handleMessage:02_3");
					throw new Exception(
							"This service requires <dsig:X509Certificate> or <dsig:GOSTKeyValue>, which is missing!!!");
				}

				NodeList publicKeyList_reply = ((Element) signatureList_reply
						.item(0)).getElementsByTagNameNS("*", "PublicKey");

				if (publicKeyList_reply == null
						|| publicKeyList_reply.getLength() == 0) {
					throw new Exception(
							"This service requires <dsig:PublicKey>, which is missing!!!");
				}

				String base64PublKey_reply = publicKeyList_reply.item(0)
						.getTextContent();

				
				byte[] bytePublKey_reply = org.apache.commons.codec.binary.Base64.decodeBase64(
						base64PublKey_reply.getBytes("UTF-8"));
				
				KeyFactory keyFactory_reply = KeyFactory
						.getInstance("GOST3410");
				EncodedKeySpec publicKeySpec_reply = new X509EncodedKeySpec(
						bytePublKey_reply);
				publicKey_reply = keyFactory_reply
						.generatePublic(publicKeySpec_reply);
			}

			if (publicKey_reply == null) {

				throw new Exception("Public key is null!!!");
			}

			Node signatureNode1_reply = signatureList_reply.item(0);

			DOMValidateContext valContext1_reply = new DOMValidateContext(
					publicKey_reply, signatureNode1_reply);

			valContext1_reply.putNamespacePrefix(XMLSignature.XMLNS, "dsig");

			valContext1_reply
					.setIdAttributeNS(
							soapHeader_reply,
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"Id");
			valContext1_reply
					.setIdAttributeNS(
							soapBody_reply,
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
							"Id");

			javax.xml.crypto.dsig.XMLSignature signature1_reply = fac
					.unmarshalXMLSignature(valContext1_reply);

			boolean result1_reply = signature1_reply
					.validate(valContext1_reply);

			System.out.println("dispatch:sign_reply_validate:" + result1_reply);

			Node asserion = ((Element) soapBody_reply.getFirstChild())
					.getElementsByTagNameNS(
							"urn:oasis:names:tc:SAML:2.0:assertion",
							"Assertion").item(0);

			Document newDoc = SamlDoc.createDocument();
			Node newNode = newDoc.importNode((Element) asserion, true);
			newDoc.appendChild(newNode);

			result = newDoc;

		} catch (Exception e) {
			System.out.println("STSTest:issueSTSSAMLToken:error:01:" + e);
			System.out.println("STSTest:issueSTSSAMLToken:error:02:"
					+ e.getMessage().startsWith(
							"ru.spb.iac.cud.exceptions.InvalidCredentials"));
			e.printStackTrace(System.out);
			
			throw e;
		}

		return result;
	}

	public static void propagateIDAttributeSetup(Node sourceNode,
			Element destElement) {
		NamedNodeMap nnm = sourceNode.getAttributes();
		for (int i = 0; i < nnm.getLength(); i++) {
			Attr attr = (Attr) nnm.item(i);
			if (attr.isId()) {
				destElement.setIdAttribute(attr.getName(), true);
				break;
			}
		}
	}
	public static Document get_saml_assertion_from_xml() {

		Document result = null;
		String sts_assertion_file = "sts_doc.xml";
		try {

			File f = new File(sts_assertion_file);
			if (f.exists()) {
				InputStream samlAssertionInputStream = new FileInputStream(f);

				DocumentBuilderFactory factory = DocumentBuilderFactory
						.newInstance();
				factory.setNamespaceAware(true);
				factory.setXIncludeAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				org.w3c.dom.Document document = builder
						.parse(samlAssertionInputStream);

				result = document;
			}

		} catch (Exception e3) {
			System.out.println("get_saml_assertion_from_xml:error:" + e3);
			e3.printStackTrace(System.out);
		}
		return result;
	}
	
}
