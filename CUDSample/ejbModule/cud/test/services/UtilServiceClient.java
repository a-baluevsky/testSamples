package cud.test.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;

import ru.spb.iac.cud.exceptions.GeneralFailure;
import ru.spb.iac.cud.items.Attribute;
import ru.spb.iac.cud.items.Function;
import ru.spb.iac.cud.items.Group;
import ru.spb.iac.cud.items.GroupsData;
import ru.spb.iac.cud.items.Resource;
import ru.spb.iac.cud.items.Role;
import ru.spb.iac.cud.items.User;
import ru.spb.iac.cud.items.UsersData;
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
public class UtilServiceClient {

	static String endpointURI = MainServicesTest.utilServiceWsdlLocationURI;

	public static void main(String[] args) {

		String user_auth_token_id = null;
		try {

			//извлекаем токен на систему из файла
			Document system_saml_assertion = MainServicesTest.get_saml_assertion_from_xml();


			resources_data(system_saml_assertion,
					user_auth_token_id);
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}
	
	/**
	 * метод получения списка подсистем
	 * 
	 */
	public static void resources_data(Document saml_assertion, String token_id)
			throws Exception {
		try {
			
				
        	//SYS - все подсистемы
			//USER - подсистемы, доступные аутентифицированному пользователю
			
			String category = "SYS";
			//String category = "USER";

			List<Resource> resources = getPort(endpointURI, saml_assertion,
					token_id).resources_data(category);
			
			for (Resource resource : resources) {

				System.out.println("resources_data:01:" + resource.getName());
				System.out.println("resources_data:02:" + resource.getCode());

				if (resource.getLinks() != null) {
					for (String link : resource.getLinks()) {
						System.out.println("resources_data:03:" + link);
					}

				}
			}

		} catch (GeneralFailure e2) {
			System.out.println("main:error2:" + e2);
		} catch (Exception e3) {
			System.out.println("main:error3:" + e3);
			e3.printStackTrace(System.out);
		}
	}

	/**
	 * роли текущего пользователя
	 * 
	 */
	public static void roles_data(Document saml_assertion, String token_id)
			throws Exception {
		try {
			
				
        	//SYS - все подсистемы
			//USER - подсистемы, доступные аутентифицированному пользователю
			
			//String category = "SYS";
			String category = "USER";

			List<Role> roles = getPort(endpointURI, saml_assertion,
					token_id).roles_data(category);

			for (Role role : roles) {

				System.out.println("roles_data:01:" + role.getName());
				System.out.println("roles_data:02:" + role.getCode());
			}

		} catch (GeneralFailure e2) {
			System.out.println("main:error2:" + e2);
		} catch (Exception e3) {
			System.out.println("main:error3:" + e3);
			e3.printStackTrace(System.out);
		}
	}
	
	/**
	 * метод получения списка пользователей
	 * 
	 */
	public static void users_data(Document saml_assertion, String token_id)
			throws Exception {
		try{
			
			//фильтр по наличию ролей
			 List <String> roles = new ArrayList<String>();
			// roles.add("urn:role:committee:admin");
			// roles.add("urn:role:cidwh:sender");
			 
			 //фильтр по ид пользователей
			  List<String> uidsUsers= new ArrayList<String>();
			 // uidsUsers.add("41");
			//  uidsUsers.add("225");
				
			  //фильтр по группам
			  List<String> groupsUsers= new ArrayList<String>();
			  //groupsUsers.add("urn:group-users:12");
			  
			  
			 //все пользователи
			  String category = "ALL";
			  //пользователи, имеющие роли в системе
			 // String category = "SYS";
			  
			  List<String> settings = new ArrayList();
			  
			  settings.add("ACCOUNTS_ONLY:TRUE");
			 settings.add("FILTER_FIO:test");
			 //settings.add("FILTER_ORG:Админ");
			  
			  UsersData isu = getPort(endpointURI, saml_assertion,
						token_id).users_data(uidsUsers, category, roles, null,  0, null, settings);
			
			  List<User> ual=isu.getUsers();
			  Integer count = isu.getCount();
			  System.out.println("users_data2:count:"+count);
			  
			  System.out.println("users_data2:size:"+ual.size());
			  
			  for(User ua :ual){
			//	  System.out.println("***************************");
			 // System.out.println("is_users:uid:"+ua.getUid());
				  
				  List<Attribute> al=ua.getAttributes();
				  for(Attribute at :al){
					if(at.getName().equals("USER_FIO")||at.getName().equals("USER_LOGIN")||at.getName().equals("ORG_NAME")) { 
					  //System.out.println("is_users:name:"+at.getName());
					  System.out.println("is_users:value:"+at.getValue());
				   } 
				  }
				  
				  List<String> idRoles=ua.getCodesRoles();
				  
				  if(idRoles!=null){
				   for(String st :idRoles){
				   System.out.println("users_data2:idRole:"+st);
				   }
			      }
				  
	             List<String> idGroups=ua.getCodesGroups();
				  
				  if(idGroups!=null){
				   for(String st :idGroups){
				 	  System.out.println("users_data2:idGroup:"+st);
				   }
			      }
				  
			  }
			  
		    }catch(GeneralFailure e2){
	        	 System.out.println("users_data2:error2:"+e2);
			}catch(Exception e3){
				 System.out.println("is_users:error3:"+e3);
			}
	}
	
	/**
	 * метод получения списка групп
	 * 
	 */
	public static void groups_data(Document saml_assertion, String token_id) throws Exception{
		try{
		
		 System.out.println("groups_data:01");
			
		  String category = "USER";
		 // String category = "SYS";
		  
		  GroupsData isu = getPort(endpointURI, saml_assertion,
					token_id).groups_data(null, category, null, 0, 50, null);
		
		  List<Group> ual=isu.getGroups();
		  Integer count = isu.getCount();
		  System.out.println("groups_data:count:"+count);
		  
		  System.out.println("users_data2:size:"+ual.size());
		  
		  for(Group ua :ual){
				  
			  System.out.println("groups_data:idGroup:"+ua.getCode());
			  
			  List<String> idRoles=ua.getCodesRoles();
			  
			  if(idRoles!=null){
			   for(String st :idRoles){
			 	  System.out.println("groups_data:idRole:"+st);
			   }
		      }
		  }
		  
	    }catch(GeneralFailure e2){
        	 System.out.println("groups_data:error2:"+e2);
		}catch(Exception e3){
			 System.out.println("groups_data:error3:"+e3);
			 e3.printStackTrace(System.out);
		}
	}
	
	/**
	 * метод получения списка функций аудита
	 * 
	 */
	public static void sys_functions(Document saml_assertion, String token_id) throws Exception{
		
		try{
			
			System.out.println("sys_functions:01");
			 
			List<Function> functions = getPort(endpointURI, saml_assertion,
					token_id).sys_functions();
		
			System.out.println("sys_functions:02:"+(functions==null));
			
	    	System.out.println("sys_functions:size:"+functions.size());
	    	
	    	for(Function function : functions){
	    		
	    		System.out.println("sys_functions:name:"+function.getName());
	    		System.out.println("sys_functions:code:"+function.getCode());
	    	}
	    	
	    }catch(GeneralFailure e2){
	    	 System.out.println("sys_functions:error2:"+e2);
		}catch(Exception e3){
			 System.out.println("sys_functions:error3:"+e3);
			 e3.printStackTrace(System.out);
		}
		
	}
	private static UtilService getPort(String endpointURI,
			Document saml_assertion, String token_id)
			throws MalformedURLException {

		
		
		//включается для HTTPS
		System.setProperty("javax.net.ssl.trustStore",
				MainServicesTest.trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", 
				MainServicesTest.trustStorePass);
		
		
		QName serviceName = new QName("http://util.services.cud.iac.spb.ru/",
				"UtilServiceImplService");
		URL wsdlURL = new URL(endpointURI);

		Service service = Service.create(wsdlURL, serviceName);

		QName portName = new QName("http://util.services.cud.iac.spb.ru/",
				"UtilServiceImplPort");

		UtilService port = service.getPort(portName, UtilService.class);
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
