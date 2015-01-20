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
import ru.spb.iac.cud.services.audit.AuditService;
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
public class AuditServiceClient {

	static String endpointURI = MainServicesTest.auditServiceWsdlLocationURI;

	public static void main(String[] args) {

		String user_id = null;
		try {

			//извлекаем токен на систему из файла
			Document system_saml_assertion = MainServicesTest.get_saml_assertion_from_xml();


			audit(system_saml_assertion,
					user_id);
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}

	}
	
	
	
	/**
	 * синхронизация списка подсистем
	 * 
	 */
	public static void audit(Document saml_assertion, String uidUser)
			throws Exception {
		try {
			
			String idFunction1 = "record:create_test";
			
			String idFunction2 = "record:update_test";
			
			  List<AuditFunction> funccodes = new ArrayList<AuditFunction>();
			   
			   AuditFunction func = new AuditFunction();
			   func.setDateFunction(new Date());
			   func.setCodeFunction(idFunction1);
			   func.setDetailsFunction("idObject:17");
			   
			   funccodes.add(func);
			   
			   func = new AuditFunction();
			   func.setDateFunction(new Date());
			   func.setCodeFunction(idFunction2);
			   func.setDetailsFunction("idAction:19");
			   
			   funccodes.add(func);
			
			getPort(endpointURI, saml_assertion).audit(uidUser, funccodes);

			

		} catch (GeneralFailure e2) {
			System.out.println("audit:error2:" + e2);
		} catch (Exception e3) {
			System.out.println("audit:error3:" + e3);
			e3.printStackTrace(System.out);
		}
	}
	 
	private static AuditService getPort(String endpointURI,
			Document saml_assertion)
			throws MalformedURLException {

		
		
		//включается для HTTPS
		System.setProperty("javax.net.ssl.trustStore",
				MainServicesTest.trustStorePath);
		System.setProperty("javax.net.ssl.trustStorePassword", 
				MainServicesTest.trustStorePass);
		
		
		QName serviceName = new QName("http://audit.services.cud.iac.spb.ru/",
				"AuditServiceImplService");
		URL wsdlURL = new URL(endpointURI);

		Service service = Service.create(wsdlURL, serviceName);

		QName portName = new QName("http://audit.services.cud.iac.spb.ru/",
				"AuditServiceImplPort");

		AuditService port = service.getPort(portName, AuditService.class);
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
	
		List<Handler> handlers = bp.getBinding().getHandlerChain();
		handlers.add(new SAML2AssertionSOAPHandler());
		handlers.add(new SignatureSOAPHandler());
		bp.getBinding().setHandlerChain(handlers);

		return port;
	}

}
