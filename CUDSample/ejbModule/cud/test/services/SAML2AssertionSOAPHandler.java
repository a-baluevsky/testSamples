package cud.test.services;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author bubnov
 * Класс - обработчик SOAP-сообщения для вставки в заголовок SAML Assertion
 *
 */
public class SAML2AssertionSOAPHandler implements
		SOAPHandler<SOAPMessageContext> {

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
			

			if (Boolean.TRUE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				handleOutbound(mc);

				sm.writeTo(System.out);
			} else {

			}

		} catch (Exception e) {
			System.out.println("SAML2AssertionSOAPHandler:handleMessage:error:"
					+ e);
			throw new ProtocolException(e);
		}
		return true;
	}

	private boolean handleOutbound(MessageContext msgContext) {

		SOAPMessageContext ctx = (SOAPMessageContext) msgContext;
		SOAPMessage soapMessage = ctx.getMessage();

	
		
		Element assertion = (Element) ctx.get("SAML2_ASSERTION");

		try {

			Document document = soapMessage.getSOAPPart();
			Element soapHeader = findOrCreateSoapHeader(document.getDocumentElement());

					
			Element wsse = document
					.createElementNS(
							"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
							"wsse:Security");
			wsse.setAttributeNS(
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
					"wsu:Id", "_id_sec");

			addNamespace(
					wsse,
					"wsse",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			addNamespace(
					wsse,
					"wsu",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");
			addNamespace(wsse, "ds", "http://www.w3.org/2000/09/xmldsig#");

			if (assertion != null) {

				wsse.appendChild(document.importNode(assertion, true));
			}

			soapHeader.insertBefore(wsse, soapHeader.getFirstChild());
		} catch (Exception e) {
			System.out
					.println("SAML2AssertionSOAPHandler:handleOutbound:error:"
							+ e);
			e.printStackTrace(System.out);
			return false;
		}
		return true;
	}

	public static void addNamespace(Element element, String prefix, String uri) {
		element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"
				+ prefix, uri);
	}

	 public static Element findOrCreateSoapHeader(Element envelope) {
	        String prefix = envelope.getPrefix();
	        String uri = envelope.getNamespaceURI();
	        QName name = new QName(uri, "Header");
	        Element header = null;
	        if( envelope.getElementsByTagNameNS(uri, "Header")!=null
	        		&& envelope.getElementsByTagNameNS(uri, "Header").getLength()>0) {
	           header = (Element)envelope.getElementsByTagNameNS(uri, "Header").item(0);
	        }
	        if (header == null) {
	            header = envelope.getOwnerDocument().createElementNS(uri, prefix + ":Header");
	            envelope.insertBefore(header, envelope.getFirstChild());
	        }

	        return header;
	    }

}
