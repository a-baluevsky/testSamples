package cud.test.services;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.ws.wssecurity.Username;
import org.opensaml.ws.wssecurity.UsernameToken;
import org.opensaml.ws.wssecurity.impl.UsernameBuilder;
import org.opensaml.ws.wssecurity.impl.UsernameMarshaller;
import org.opensaml.ws.wssecurity.impl.UsernameTokenBuilder;
import org.opensaml.ws.wssecurity.impl.UsernameTokenMarshaller;
import org.opensaml.ws.wssecurity.impl.UsernameTokenUnmarshaller;
import org.opensaml.ws.wssecurity.impl.UsernameUnmarshaller;
import org.opensaml.ws.wstrust.KeyType;
import org.opensaml.ws.wstrust.OnBehalfOf;
import org.opensaml.ws.wstrust.RequestSecurityToken;
import org.opensaml.ws.wstrust.RequestType;
import org.opensaml.ws.wstrust.TokenType;
import org.opensaml.ws.wstrust.UseKey;
import org.opensaml.ws.wstrust.impl.KeyTypeBuilder;
import org.opensaml.ws.wstrust.impl.KeyTypeMarshaller;
import org.opensaml.ws.wstrust.impl.KeyTypeUnmarshaller;
import org.opensaml.ws.wstrust.impl.OnBehalfOfBuilder;
import org.opensaml.ws.wstrust.impl.OnBehalfOfMarshaller;
import org.opensaml.ws.wstrust.impl.OnBehalfOfUnmarshaller;
import org.opensaml.ws.wstrust.impl.RequestSecurityTokenBuilder;
import org.opensaml.ws.wstrust.impl.RequestSecurityTokenMarshaller;
import org.opensaml.ws.wstrust.impl.RequestSecurityTokenUnmarshaller;
import org.opensaml.ws.wstrust.impl.RequestTypeBuilder;
import org.opensaml.ws.wstrust.impl.RequestTypeMarshaller;
import org.opensaml.ws.wstrust.impl.RequestTypeUnmarshaller;
import org.opensaml.ws.wstrust.impl.TokenTypeBuilder;
import org.opensaml.ws.wstrust.impl.TokenTypeMarshaller;
import org.opensaml.ws.wstrust.impl.TokenTypeUnmarshaller;
import org.opensaml.ws.wstrust.impl.UseKeyBuilder;
import org.opensaml.ws.wstrust.impl.UseKeyMarshaller;
import org.opensaml.ws.wstrust.impl.UseKeyUnmarshaller;
import org.opensaml.xml.XMLObjectBuilder;
import org.opensaml.xml.io.Marshaller;
import org.opensaml.xml.io.MarshallerFactory;
import org.opensaml.xml.signature.X509Certificate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class SamlDoc {

	public static Document getWSTrustRequestToken(String cert_base64,
			String token_obo) {

		Document result = null;

		try {

			DefaultBootstrap.bootstrap();

			Configuration.registerObjectProvider(
					RequestSecurityToken.ELEMENT_NAME,
					new RequestSecurityTokenBuilder(),
					new RequestSecurityTokenMarshaller(),
					new RequestSecurityTokenUnmarshaller());

			Configuration.registerObjectProvider(RequestType.ELEMENT_NAME,
					new RequestTypeBuilder(), new RequestTypeMarshaller(),
					new RequestTypeUnmarshaller());

			Configuration.registerObjectProvider(TokenType.ELEMENT_NAME,
					new TokenTypeBuilder(), new TokenTypeMarshaller(),
					new TokenTypeUnmarshaller());

			Configuration.registerObjectProvider(KeyType.ELEMENT_NAME,
					new KeyTypeBuilder(), new KeyTypeMarshaller(),
					new KeyTypeUnmarshaller());

			Configuration.registerObjectProvider(UseKey.ELEMENT_NAME,
					new UseKeyBuilder(), new UseKeyMarshaller(),
					new UseKeyUnmarshaller());

			Configuration.registerObjectProvider(OnBehalfOf.ELEMENT_NAME,
					new OnBehalfOfBuilder(), new OnBehalfOfMarshaller(),
					new OnBehalfOfUnmarshaller());

			Configuration.registerObjectProvider(UsernameToken.ELEMENT_NAME,
					new UsernameTokenBuilder(), new UsernameTokenMarshaller(),
					new UsernameTokenUnmarshaller());

			Configuration.registerObjectProvider(Username.ELEMENT_NAME,
					new UsernameBuilder(), new UsernameMarshaller(),
					new UsernameUnmarshaller());

			System.out.println("SamlTest:getWSTrustRequestToken:02");

			RequestSecurityToken rst = create(RequestSecurityToken.class,
					RequestSecurityToken.ELEMENT_NAME);
			String context = "default-context";
			rst.setContext(context);

			RequestType requestType = create(RequestType.class,
					RequestType.ELEMENT_NAME);
			requestType.setValue(RequestType.ISSUE);
			rst.getUnknownXMLObjects().add(requestType);

			TokenType tokenType = create(TokenType.class,
					TokenType.ELEMENT_NAME);
			tokenType
					.setValue("http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0");
			rst.getUnknownXMLObjects().add(tokenType);

			if (cert_base64 != null) {

				KeyType keyType = create(KeyType.class, KeyType.ELEMENT_NAME);
				keyType.setValue(KeyType.PUBLIC_KEY);
				rst.getUnknownXMLObjects().add(keyType);

				UseKey useKey = create(UseKey.class, UseKey.ELEMENT_NAME);
				rst.getUnknownXMLObjects().add(useKey);

				X509Certificate x509Certificate = create(X509Certificate.class,
						X509Certificate.DEFAULT_ELEMENT_NAME);
				x509Certificate.setValue(cert_base64);
				useKey.setUnknownXMLObject(x509Certificate);
			}

			if (token_obo != null) {

				OnBehalfOf onBehalfOf = create(OnBehalfOf.class,
						OnBehalfOf.ELEMENT_NAME);
				rst.getUnknownXMLObjects().add(onBehalfOf);

				UsernameToken usernameToken = create(UsernameToken.class,
						UsernameToken.ELEMENT_NAME);
				onBehalfOf.setUnknownXMLObject(usernameToken);

				usernameToken.setWSUId("_id_obo_ut");

				Username username = create(Username.class,
						Username.ELEMENT_NAME);

				username.setValue(token_obo);
				usernameToken.setUsername(username);
			}

			MarshallerFactory marshallerFactory = Configuration
					.getMarshallerFactory();
			Marshaller marshaller = marshallerFactory.getMarshaller(rst);
			Element assertionElement = marshaller.marshall(rst);

			result = assertionElement.getOwnerDocument();

			System.out.println("SamlTest:getWSTrustRequestToken:0100");
		} catch (Exception e) {
			System.out.println("SamlTest:getWSTrustRequestToken:error:" + e);
			e.printStackTrace(System.out);
		}

		return result;
	}

	private static <T> T create(Class<T> cls, QName qname) {
		return (T) ((XMLObjectBuilder) Configuration.getBuilderFactory()
				.getBuilder(qname)).buildObject(qname);
	}

	public static Document createDocument() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setXIncludeAware(true);

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
		} catch (Exception e) {
			throw e;
		}
		return builder.newDocument();
	}
}
