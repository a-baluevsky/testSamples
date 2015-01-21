package cud.test.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author bubnov
 * Основной класс реализации полного процесса получения токена пользователя в другой системе 
 * и вызова справочного сервиса
 *
 */
public class MainServicesTest {

	private static String sts_assertion_file = "saml_asserion.xml";

	public static String user_login = "test1";
	public static String user_password = "6KjDk8kh";
	
	//public static String user_login = "bun";
	//public static String user_password = "tor1A";
	
	//public static String user_login = "afrolov";
	//public static String user_password = "afrolov";
	
	
	//public static String user_login = "stepanov";
	//public static String user_password = "pavelS";
	
	
	//public static String system_code = "urn:eis:toris:reklama";
	//public static String system_code = "urn:eis:testcode1";
// public static String system_code = "urn:eis:to:tenancy";
// public static String system_code = "urn:group-eis:toris";
// public static String system_code = "urn:eis:toris";
 
 //public static String system_code = "urn:eis:toris:zemuch";
    
 public static String system_code = "urn:sub-eis:web:test";
    
	//public static String system_code = "urn:eis:cud";
	
	public static String onbehalfof_system_code = "urn:eis:to:adds_prms";

 // https://acc.lan.iac.spb.ru:8443
   public static String stsWsdlLocationURI = "https://acc.toris.vpn:8443/CudServicesSTS/CUDSTS?wsdl";
    public static String utilServiceWsdlLocationURI = "https://acc.toris.vpn:8443/CudServicesPro/UtilService?wsdl";
    public static String adminServiceWsdlLocationURI = "https://acc.toris.vpn:8443/CudServicesPro/AdminService?wsdl";
    public static String auditServiceWsdlLocationURI = "https://acc.toris.vpn:8443/CudServicesPro/AuditService?wsdl";


/*
    public static String stsWsdlLocationURI = "http://10.128.66.82:1780/CudServicesSTS/CUDSTS?wsdl";
   // public static String stsWsdlLocationURI = "http://10.128.66.82:1780/CudServicesSTSLong/CUDSTS?wsdl";
    public static String utilServiceWsdlLocationURI = "http://10.128.66.82:1780/CudServicesPro/UtilService?wsdl";
    public static String adminServiceWsdlLocationURI = "http://10.128.66.82:1780/CudServicesPro/AdminService?wsdl";
    public static String auditServiceWsdlLocationURI = "http://10.128.66.82:1780/CudServicesPro/AuditService?wsdl";
*/
/*
   // public static String stsWsdlLocationURI = "http://10.146.136.31:8080/CudServicesSTS/CUDSTS?wsdl";
    public static String stsWsdlLocationURI = "http://10.146.136.31:8080/CudServicesSTSLong/CUDSTS?wsdl";
    public static String utilServiceWsdlLocationURI = "http://10.146.136.31:8080/CudServicesPro/UtilService?wsdl";
    public static String adminServiceWsdlLocationURI = "http://10.146.136.31:8080/CudServicesPro/AdminService?wsdl";
    public static String auditServiceWsdlLocationURI = "http://10.146.136.31:8080/CudServicesPro/AuditService?wsdl";
*/
    
    public static String trustStorePath = "uc.store";
	
	public static String trustStorePass = "123";
	
	//public static char[] signingKeyPass = "1234567890".toCharArray();
  // public static String signingAlias = "RaUser-0820764f-ebd6-4e96-a96f-c48719d29059";
	
	public static char[] signingKeyPass = "Access_Control".toCharArray();
	public static String signingAlias = "cudvm_export";
		
	//public static char[] signingKeyPass = "1234567890".toCharArray();
	//public static String signingAlias = "toris";
	
	
	public static void main(String[] args) {

			
	//	System.exit(0);
		//полный вызов получения токена пользователя в другой системе
		//полный вызов сервисов
		
		//1.обращение к STS - аутентификация системы
		//2.обращение к STS - аутентификация пользователя
		//3.получение токен пользователя в другой системе
		//4. вызов сервисов
		
		Document user_saml_assertion = null;
		String user_auth_token_id = null;
		String user_uid = null;
		Document user_onbehalfof_saml_assertion = null;
		
		try {

			//извлекаем токен на систему из файла
			Document system_saml_assertion = get_saml_assertion_from_xml();

			//проверка токена на истечение срока действия
			if (system_saml_assertion == null
					|| assertion_expired(system_saml_assertion)) {
				
				//если у токена истёк срок действия
				//делаем вызов STS для получения нового токена
				system_saml_assertion = STSClient.issueSystemToken();
				print(system_saml_assertion);
				
				save_saml_assertion_to_xml(system_saml_assertion);
			}
		
			//получаем токен на пользователя
			user_saml_assertion = STSClient.issueUserToken();
			print(user_saml_assertion);
		
			
			//Thread.sleep(10000);
			
			//выбираем аттрибут TOKEN_ID
			user_auth_token_id = get_token_id(user_saml_assertion);
          //получаем токен пользователя в другой системе (onbehalfof_system_code)
			//на основе имеющегося токена
			//user_onbehalfof_saml_assertion = STSClient.issueUserOnBehalfOfToken(
			//		user_auth_token_id);
			//print(user_onbehalfof_saml_assertion);
			
			
			//выполняем вызов метода получения подсистем 
		//	UtilServiceClient.resources_data(system_saml_assertion,
		//			user_auth_token_id);

			//выполняем вызов метода получения групп текущего пользователя
			//UtilServiceClient.groups_data(system_saml_assertion,
			//		user_auth_token_id);
			
			//выполняем вызов метода получения списка пользователей
			UtilServiceClient.users_data(system_saml_assertion,
					user_auth_token_id);
		
			//выполняем вызов метода получения ролей текущего пользователя
			//UtilServiceClient.roles_data(system_saml_assertion,
			//		user_auth_token_id);
			
		    //выполняем вызов метода синхронизации подсистем 
			//AdminServiceClient.sync_resources(system_saml_assertion,
			//		user_auth_token_id);
			
			//выполняем вызов метода синхронизации ролей 
			//AdminServiceClient.sync_roles(system_saml_assertion,
			//		user_auth_token_id);
			
			// метод получения списка функций аудита
			//UtilServiceClient.sys_functions(system_saml_assertion,
		    // user_auth_token_id);
					
			//выполняем вызов метода синхронизации функций аудита 
			//AdminServiceClient.sync_functions(system_saml_assertion,
			//		user_auth_token_id);
			
			//выбираем аттрибут USER_UID
			//user_uid = get_user_uid(user_saml_assertion);
			//выполняем вызов метода аудита действий пользователя
			//AuditServiceClient.audit(system_saml_assertion, user_uid);
			
			// Andrey Baluevsky: custom method call: testAB
			AuditServiceClient.audit(system_saml_assertion, user_uid);
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}

	public static Document get_saml_assertion_from_xml() {

		Document result = null;

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

	private static void save_saml_assertion_to_xml(Document saml_assertion) {

		InputStream in = null;
		OutputStream output = null;
		byte[] buffer = new byte[4096];
		int n = -1;

		try {

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(saml_assertion);
			StreamResult saml_stream = new StreamResult(new StringWriter());
			transformer.transform(source, saml_stream);
			String samlMessage = saml_stream.getWriter().toString();

			File file = new File(sts_assertion_file);

			in = new ByteArrayInputStream(samlMessage.getBytes("UTF-8"));

			output = new FileOutputStream(file);

			while ((n = in.read(buffer)) != -1) {
				if (n > 0) {
					output.write(buffer, 0, n);
				}
			}
			output.close();

		} catch (Exception e3) {
			System.out.println("save_saml_assertion_to_xml:error:" + e3);
			e3.printStackTrace(System.out);
		}
	}

	private static boolean assertion_expired(Document saml_assertion) {

		boolean result = false;

		try {

			Node condition = saml_assertion.getElementsByTagNameNS(
					"urn:oasis:names:tc:SAML:2.0:assertion", "Conditions")
					.item(0);

			XMLGregorianCalendar notOnOrAfter = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(
							((Element) condition).getAttribute("NotOnOrAfter"));

			result = notOnOrAfter.toGregorianCalendar().getTime()
					.before(new Date());

			System.out.println("assertion_expired:02:"
					+ notOnOrAfter.toGregorianCalendar().getTime());
			System.out.println("assertion_expired:03:" + new Date());

			System.out.println("assertion_expired:04:" + result);

		} catch (Exception e3) {
			System.out.println("get_saml_assertion_from_xml:error:" + e3);
			e3.printStackTrace(System.out);
		}
		return result;
	}

	private static String get_token_id(Document saml_assertion) {

		String result = null;

		try {

			NodeList user_attributes = saml_assertion.getElementsByTagNameNS(
					"urn:oasis:names:tc:SAML:2.0:assertion", "Attribute");

			for (int i = 0; i < user_attributes.getLength(); i++) {
				Node n = user_attributes.item(i);

				if ("TOKEN_ID".equals(((Element) n).getAttribute("Name"))) {

					result = n.getFirstChild().getTextContent();
				}
			}

		} catch (Exception e3) {
			System.out.println("get_token_id:error:" + e3);
			e3.printStackTrace(System.out);
		}
		return result;
	}
	private static String get_user_uid(Document saml_assertion) {

		String result = null;

		try {

			NodeList user_attributes = saml_assertion.getElementsByTagNameNS(
					"urn:oasis:names:tc:SAML:2.0:assertion", "Attribute");

			for (int i = 0; i < user_attributes.getLength(); i++) {
				Node n = user_attributes.item(i);

				if ("USER_UID".equals(((Element) n).getAttribute("Name"))) {

					result = n.getFirstChild().getTextContent();
				}
			}

		} catch (Exception e3) {
			System.out.println("get_user_uid:error:" + e3);
			e3.printStackTrace(System.out);
		}
		return result;
	}
	
	private static void print(Document saml_assertion) {

		try {

			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(saml_assertion);
			StreamResult saml_stream = new StreamResult(new StringWriter());
			transformer.transform(source, saml_stream);
			String samlMessage = saml_stream.getWriter().toString();

			System.out.println("print:" + samlMessage);

		} catch (Exception e3) {
			System.out.println("print:error:" + e3);
			e3.printStackTrace(System.out);
		}
	}

}
