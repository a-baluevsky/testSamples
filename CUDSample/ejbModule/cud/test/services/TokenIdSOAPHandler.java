package cud.test.services;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.w3c.dom.Node;

/**
 * 
 * @author bubnov
 * Класс - обработчик SOAP-сообщения для вставки в заголовок TokenID
 *
 */
public class TokenIdSOAPHandler implements SOAPHandler<SOAPMessageContext> {

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
			sm.writeTo(System.out);

			SOAPHeader header = sm.getSOAPHeader();
			SOAPBody body = sm.getSOAPBody();

			// запрос
			if (Boolean.TRUE.equals(mc
					.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {

				Node SecuritySOAP = header.getFirstChild();

				SOAPMessageContext ctx = (SOAPMessageContext) mc;

				String UserAuthTokenId = (String) ctx.get("TOKEN_ID");

				QName LoginTokenQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"UsernameToken", "wsse");
				QName LoginQN = new QName(
						"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd",
						"Username", "wsse");
				SOAPElement LoginTokenSOAP = ((SOAPElement) SecuritySOAP)
						.addChildElement(LoginTokenQN);
				SOAPElement LoginSOAP = LoginTokenSOAP.addChildElement(LoginQN);
				// обязательно
				LoginSOAP
						.addNamespaceDeclaration(
								"wsse",
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
				LoginTokenSOAP
						.setAttributeNS(
								"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
								"wsu:Id", "UserAuthTokenId");
				LoginSOAP.addTextNode(UserAuthTokenId);

			} else {
				// ответ

			}

		} catch (Exception e) {
			System.out.println("TokenIDSOAPHandler:handleMessage:error:" + e);
			throw new ProtocolException(e);
		}
		return true;
	}

}
