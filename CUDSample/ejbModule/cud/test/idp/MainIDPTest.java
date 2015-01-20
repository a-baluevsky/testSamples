package cud.test.idp;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;



public class MainIDPTest {

   private static boolean signRequired = true;
	
	public static void main(String[] args) {

		
		
		//формирование строки SAML-запроса при GET binding
		
		String url_get_idp = null;
		try {

			url_get_idp = "http://localhost:8080/cudidp/login?" + test1();

			System.out.println("url_get_idp:" + url_get_idp);

		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}

	private static String test1() throws IOException {

		String result = null;
		char[] signingKeyPass = "1234567890".toCharArray();;
		String signingAlias = "RaUser-0820764f-ebd6-4e96-a96f-c48719d29059";
	
		String url1 = null;
		String url2 = null;
		String relayState = null;
		try {

			Document samlDocument = get_saml_assertion_from_xml();

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(samlDocument);
			StreamResult saml_stream = new StreamResult(new StringWriter());
			transformer.transform(source, saml_stream);
			String samlMessage = saml_stream.getWriter().toString();

			String base64Request = deflateBase64URLEncode(samlMessage
					.getBytes("UTF-8"));

		 if (signRequired) {
			KeyStore ks = KeyStore.getInstance("HDImageStore", "JCP");
			ks.load(null, null);

			PrivateKey signingKey = (PrivateKey) ks.getKey(signingAlias,
					signingKeyPass);

			
			
			byte[] sigValue = computeSignature("SAMLRequest", base64Request,
					relayState, signingKey);
			url2 = getRedirectURLWithSignature("SAMLRequest", base64Request,
					relayState, sigValue, signingKey.getAlgorithm());

			
			result = url2;
			
		   }else{
			   result = "SAMLRequest="+base64Request;
		   }
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

		return result;
	}

	private static String getRedirectURLWithSignature(String samlParameter,
			String urlEncoded, String urlEncodedRelayState, byte[] signature,
			String sigAlgo) throws IOException {

		StringBuilder sb = new StringBuilder();

		addParameter(sb, samlParameter, urlEncoded);

		String sigAlg = "GOST3411withGOST3410EL";

		sigAlg = URLEncoder.encode(sigAlg, "UTF-8");

		addParameter(sb, "SigAlg", sigAlg);

		String encodedSig = base64URLEncode(signature);

		addParameter(sb, "Signature", encodedSig);

		return sb.toString();
	}

	public static InputStream decode(byte[] msgToDecode) {
		ByteArrayInputStream bais = new ByteArrayInputStream(msgToDecode);
		return new InflaterInputStream(bais, new Inflater(true));
	}

	public static String deflateBase64URLEncode(String stringToEncode)
			throws IOException {
		return deflateBase64URLEncode(stringToEncode.getBytes("UTF-8"));
	}

	public static String deflateBase64URLEncode(byte[] stringToEncode)
			throws IOException {
		byte[] deflatedMsg = encode(stringToEncode);
		return base64URLEncode(deflatedMsg);
	}

	public static byte[] encode(byte[] message) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Deflater deflater = new Deflater(Deflater.DEFLATED, true);
		DeflaterOutputStream deflaterStream = new DeflaterOutputStream(baos,
				deflater);
		deflaterStream.write(message);
		deflaterStream.finish();

		return baos.toByteArray();
	}

	public static String base64URLEncode(byte[] stringToEncode)
			throws IOException {
		String base64Request = new String(
				org.apache.commons.codec.binary.Base64.encodeBase64(
						stringToEncode, false));

		return urlEncode(base64Request);
	}

	public static String urlEncode(String str) throws IOException {
		return URLEncoder.encode(str, "UTF-8");
	}

	private static byte[] computeSignature(String samlParameter,
			String urlEncoded, String urlEncodedRelayState,
			PrivateKey signingKey) throws Exception {

		StringBuilder sb = new StringBuilder();

		addParameter(sb, samlParameter, urlEncoded);

		String sigAlg = "GOST3411withGOST3410EL";

		sigAlg = URLEncoder.encode(sigAlg, "UTF-8");

		addParameter(sb, "SigAlg", sigAlg);

		byte[] sigValue = sign(sb.toString(), signingKey);

		return sigValue;
	}

	private static void addParameter(StringBuilder queryString,
			String paramName, String paramValue) {
		String parameterSeparator = "&";

		if (queryString.length() == 0) {
			parameterSeparator = "";
		}

		queryString.append(parameterSeparator).append(paramName).append("=")
				.append(paramValue);
	}

	private static Document get_saml_assertion_from_xml() {

		Document result = null;

		try {

			InputStream samlAssertionInputStream = new FileInputStream(
					"saml_request.xml");

			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			org.w3c.dom.Document document = builder
					.parse(samlAssertionInputStream);

			result = document;

		} catch (Exception e3) {
			System.out.println("get_saml_assertion_from_xml:error:" + e3);
			e3.printStackTrace(System.out);
		}
		return result;
	}

	public static byte[] sign(String stringToBeSigned, PrivateKey signingKey)
			throws Exception {
		if (stringToBeSigned == null)
			throw new Exception("stringToBeSigned");
		if (signingKey == null)
			throw new Exception("signingKey");

		Signature sig = Signature.getInstance("GOST3411withGOST3410EL");

		sig.initSign(signingKey);
		sig.update(stringToBeSigned.getBytes());
		return sig.sign();
	}

}
