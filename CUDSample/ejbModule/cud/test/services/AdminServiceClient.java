package cud.test.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.AuditFunction;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Group;
import ru.spb.iac.cud.items.GroupsData;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.User;
import ru.spb.iac.cud.items.UsersData;
import ru.spb.iac.cud.services.admin.AdminService;
import ru.spb.iac.cud.services.util.UtilService;

import org.w3c.dom.Document;
/*
import ru.CryptoPro.ssl.SSLSocketFactoryImpl;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
*/
/**
 * 
 * @author bubnov
 * Клиент вызова справочного сервиса
 *
 */
public class AdminServiceClient {

	static String endpointURI = MainServicesTest.adminServiceWsdlLocationURI;

	public static void main(String[] args) {

		String user_auth_token_id = null;
		try {

			//извлекаем токен на систему из файла
			Document system_saml_assertion = MainServicesTest.get_saml_assertion_from_xml();


			sync_resources(system_saml_assertion,
					user_auth_token_id);
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}
	
	/**
	 * синхронизация списка подсистем
	 * 
	 */
	public static void sync_resources(Document saml_assertion, String token_id)
			throws Exception {
		try {
			
				
        	//SYS - все подсистемы
			//USER - подсистемы, доступные аутентифицированному пользователю
			
			String modeExec = "ADD";
		
			List<Resource> rlist = new ArrayList<Resource>();
			List<String> links = new ArrayList<String>();
			Resource res = new Resource();
			
			/*res.setCode("urn:eis:testcode1");
			res.setName("ТестСистема1");
			
			links.add("http://proxy1.iac.spb.ru");
			links.add("http://cache1.iac.spb.ru");
			
			res.setLinks(links);
			
			rlist.add(res);
			
            res = new Resource();
			*/
			res.setCode("urn:eis:testcode2");
			res.setName("ТестСистема2+");
			
			links = new ArrayList<String>();
			links.add("http://proxy2.iac.spb.ru");
			links.add("http://cache2.iac.spb.ru");
			
			res.setLinks(links);
			
			rlist.add(res);
			
			getPort(endpointURI, saml_assertion,
					token_id).sync_resources(rlist, modeExec);

			

		} catch (GeneralFailure e2) {
			System.out.println("main:error2:" + e2);
		} catch (Exception e3) {
			System.out.println("main:error3:" + e3);
			e3.printStackTrace(System.out);
		}
	}
	
	/**
	 * синхронизация ролей подсистемы
	 * 
	 */
	public static void sync_roles(Document saml_assertion, String token_id)
			throws Exception {
		try {
			
			//0 - REPLACE
		   	//1 - ADD
		   	//2 - REMOVE 
			String modeExec = "ADD";
			  
		    List<Role> roles = new ArrayList<Role>();
		  
		    Role role = new Role();
		    role.setCode("urn:role:test_role1");
		    role.setName("Роль оператора тестовая 1");
		    role.setDescription("Управление полномочиями");
	    	roles.add(role);
	    	
	    	role = new Role();
	    	role.setCode("urn:role:test_role2");
	    	role.setName("Роль оператора тестовая 2");
	    	role.setDescription("Управление полномочиями");
	    	roles.add(role);
	    	
	    	getPort(endpointURI, saml_assertion,
					token_id).sync_roles(roles, modeExec);	

		} catch (GeneralFailure e2) {
			System.out.println("main:error2:" + e2);
		} catch (Exception e3) {
			System.out.println("main:error3:" + e3);
			e3.printStackTrace(System.out);
		}
	}
	
	/**
	 * Синхронизация функций аудита
	 * 
	 */
	 public static void sync_functions(Document saml_assertion, String token_id) throws Exception{
			
		   	List<Function> functions = new ArrayList<Function>();
		
		   	Function func = new Function();
		   	func.setCode("record:create_test");
		   	func.setName("Создание записи тест");
		   	func.setDescription("Создание записи "); 
		   	functions.add(func);
		   	
		   	func = new Function();
		   	func.setCode("record:update_test");
		   	func.setName("Редактирование записи тест");
		   	func.setDescription("Редактирование записи ");
		   	functions.add(func);
		 
		
		   	try{
		   		
		   	    //0 - REPLACE
			   	//1 - ADD
			   	//2 - REMOVE 
				String modeExec = "ADD";
				
				getPort(endpointURI, saml_assertion,
						token_id).sync_functions(functions, modeExec);
		   	  
		   	}catch(GeneralFailure e1){
		      	  System.out.println("main:error1:"+e1);
		      	  System.out.println("main:error1:"+e1.getMessage());
				}catch(Exception e2){
				  System.out.println("main:error2:"+e2);
				}
		   }
	 
	private static AdminService getPort(String endpointURI,
			Document saml_assertion, String token_id)
			throws MalformedURLException {

		
		
		//включается для HTTPS
		System.setProperty("javax.net.ssl.trustStore",
				MainServicesTest.trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", 
				MainServicesTest.trustStorePass);
		
		
		QName serviceName = new QName("http://admin.services.cud.iac.spb.ru/",
				"AdminServiceImplService");
		URL wsdlURL = new URL(endpointURI);

		Service service = Service.create(wsdlURL, serviceName);

		QName portName = new QName("http://admin.services.cud.iac.spb.ru/",
				"AdminServiceImplPort");

		AdminService port = service.getPort(portName, AdminService.class);
/*
	   HTTPConduit httpConduit = (HTTPConduit) ClientProxy.getClient(port)
				.getConduit();
		TLSClientParameters tlsCP = new TLSClientParameters();
		final SSLSocketFactoryImpl sslFact = new SSLSocketFactoryImpl();
		tlsCP.setSSLSocketFactory(sslFact);
		tlsCP.setDisableCNCheck(true);
		httpConduit.setTlsClientParameters(tlsCP);
*/
		BindingProvider bp = (BindingProvider) port;
		bp.getRequestContext().put("SAML2_ASSERTION",
				saml_assertion.getDocumentElement());
		bp.getRequestContext().put("TOKEN_ID", token_id);

		List<Handler> handlers = bp.getBinding().getHandlerChain();
		handlers.add(new SAML2AssertionSOAPHandler());
		handlers.add(new TokenIdSOAPHandler());
		handlers.add(new SignatureSOAPHandler());
		bp.getBinding().setHandlerChain(handlers);

		return port;
	}

}
