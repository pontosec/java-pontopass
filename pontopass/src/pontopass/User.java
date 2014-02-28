package pontopass;
/****************************************************************************                                                                          
*                     PontoSec Segurança da Informação                      *
*                     														*
*  @Produto			Pontopass												*
*  @Código			PPAPIJ02	                                        	*
*  @Projeto			API: Java  			   									*
*  @Programa		Interface para gerenciamento de dados do usuário		*
*  @Author  	    Guilherme Cesar Leite                               	*
*  @version			17/05/2013					                        	*
*                                                                           *
*  User.java																*
*  Descrição:																*
*  Esta classe possui funcionalidades para gerenciamento de dados via API.  *						
*  Dependencias:															*
*  Os retornos das funções essencialmente serão do tipo inteiro. Porém,		*
*  para alguns casos, o retorno será feito em JSONObject ou JSONArray.		*
*  Esta classe dispões de funções para converter esses dados, porém as 	    *
*  classes para correto tratamento do JSON deverão estar disponíveis		*
*																			*								
*																			*
*  Histórico:																*
*	Versão	Data			Desenvolvedor	 Cod  Descrição	                *
*	1		17/05/2013		Guilherme Leite  ---  Versão inicial		    *
*	1.0.1	18/07/2013		Guilherme Leite	 ---  Tratamento de Exceções	*
*																			*
****************************************************************************/
import java.io.IOException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class User extends Auth {

	public String app_user, app_pass;
	private String session,ip,user_agent;
	private String login = null;
	public int lastError;
	/*public static void main(String[] args) {
		Manage teste = new Manage("teste","teste");
		try {
			JSONArray deviceList = teste.listDevices("guilherme@pontosec.com");
			int res = 999;
			res  = teste.createDevice("guilherme@pontosec.com",1, "5511991885210", "teste_API_JAVA");
			res = teste.deleteDevice("guilherme@pontosec.com", res);
			res = teste.createUser("teste@api.com","api_java");
			res = teste.deleteUser("kamacho@bolado");
		} catch (ParseException | org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}*/
	
	
	public User(){
	}
	
	public User(Auth api){
		app_user = api.app_user;
		app_pass = api.app_pass;
		login = api.usrLogin;
	}
	
	public User(String user_par, String pass_par){
		app_user = user_par;
		app_pass = pass_par;
	}
	
    /**
     * Define um login padrão para as operaçõeso<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário<br/> 
     */
	public void setLogin(String login_var){
		login = login_var;
	}
	
	
	
    /**
     * Cria um novo método de autenticação para o usuário desejado<br/>
     * Requisitos:<br/>
	 * 		<p>Usuário criado<br/> Login padrão definido</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * Int method: ID do método, obtido no array recebido do List<br/>
	 * String phone: Número de telefone (incluindo DDI e DDD)
	 * String desc: Descrição do método
     * Saída------------------------------------------------------------------------------<br/>
     * Int MethodID: Id do método cadastrado, ou 0 em caso de erro
     */
	public int addDevice(int method_par, String phone_par, String desc_par){
		return (login != null ? addDevice(login,method_par,phone_par,desc_par) : 0);
	}
	
    /**
     * Cria um novo método de autenticação para o usuário desejado<br/>
     * Requisitos:<br/>
	 * 		<p>Usuário criado</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário que terá o método cadastrado
	 * Int method: ID do método<br/>
	 * String phone: Número de telefone (incluindo DDI e DDD) <br/>
	 * String desc: Descrição do método<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Int MethodID: Id do método cadastrado, ou 0 em caso de erro
     * <i>OBS: para os métodos 3 e 4 (mobile), basta criar apenas um deles, o sistema automaticamente
     * criará o outro. Eles terão o ID seguidos, e será retornado o menor deles.</i>
     */
	 public int addDevice(String login_par, int method_par, String phone_par, String desc_par){
		int status = 999;
		int methodId = 999;
		desc_par = desc_par.replace(" ", "%20");
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/method/insert/" + login_par 
										+ "/" + method_par + "/" + phone_par + "/" + desc_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do createDev
				JSONObject cDev_json;
				try {
					cDev_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					lastError = status;
					return 0;
				}
				//verifica o status
				status = Integer.valueOf(Long.toString((Long)cDev_json.get("status")));
				if (status == 0){
					//grava o ID do método criado
					methodId = Integer.valueOf(Long.toString((Long)cDev_json.get("method_id")));
					if ((method_par == 3) || (method_par == 4)){
						int methodId2 = Integer.valueOf(Long.toString((Long)cDev_json.get("method_id_2")));
						methodId = (methodId > methodId2 ? methodId2 : methodId );
					}
				}
				else lastError = status;
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return 0;
		} 
		return (status == 0 ? methodId : 0);
	}
	
    /**
     * Remove o método de autenticação<br/>
     * Requisitos:<br/>
	 * 		<p>Login padrão definido</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * Int method: ID do método<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Int StatusCode: Código do status do processo. 0: OK
     */
	public int deleteDevice(int method_par) {
		return (login != null ? deleteDevice(login,method_par) : 999);
	}
	
    /**
     * Remove o método de autenticação<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário<br/>
	 * Int method: ID do método<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Int StatusCode: Código do status do processo. 0: OK
     */
	public int deleteDevice(String login_par, int method_par){
		int status = 999;
		int methodId = 999;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/method/delete/" + login_par 
										+ "/" + method_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do deleteDev
				JSONObject dDev_json;
				try {
					dDev_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					return -999;
				}
				//verifica o status
				status = Integer.valueOf(Long.toString((Long)dDev_json.get("status")));
				if (status != 0){
					//grava o status de erro
					lastError = status;
				}
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return status;
		} 
		return status;
	}
	
    /**
     * Lista os métodos de autenticação do usuário<br/>
     * Requisitos:<br/>
	 * 		<p>Login padrão definido</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * JSONArray devices: Array JSON contendo os métodos de autenticação do usuário
     */
	public JSONArray listDevices() {
		return (login != null ? listDevices(login) : null);
	}

	
	
    /**
     * Lista os métodos de autenticação do usuário<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * JSONArray devices: Array JSON contendo os métodos de autenticação do usuário
     */
	public JSONArray listDevices(String login_par) {
		int status = 999;
		int methodId = 999;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/method/list/" + login_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do listDev
				JSONArray device_array;
				try {
					device_array = (JSONArray)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					return null;
				}
				//JSONArray device_array =  (JSONArray)new JSONParser().parse(lDev_json.get("loginmethods").toString());
				//JSONObject device = new JSONObject();
				return device_array;
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return null;
		} 
		return null;	
	}
	
    /**
     * Cria um novo usuário<br/>
     * O nome de exibição será igual o login. <br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login desejado para o usuário<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * int userID: ID do usuário criado, 0 em caso de erro
     */
	public int create(String login_par) {
		return create(login_par,login_par);
	}
	
    /**
     * Cria um novo usuário<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login desejado para o usuário<br/>
	 * String name: Nome de exibição do usuário</br>
     * Saída------------------------------------------------------------------------------<br/>
     * int userID: ID do usuário criado, 0 em caso de erro
     */
	public int create(String login_par, String name_par) {
		int status = 999;
		int userId = 999;
		name_par = name_par.replace(" ", "%20");
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/user/insert/" + login_par + "/" + name_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do createUser
				JSONObject cUsr_json;
				try {
					cUsr_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					return -999;
				}
				status = Integer.valueOf(Long.toString((Long)cUsr_json.get("status")));
				if (status == 0){
					//grava o user ID criado
					userId = Integer.valueOf(Long.toString((Long)cUsr_json.get("user_id")));
				}
				else lastError = status;
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return 0;
		} 
		return (status == 0 ? userId : 0);	
	}
	
    /**
     * Deleta um usuário<br/>
     * Requisitos:<br/>
	 * 		<p>Login padrão definido</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * int statusCode: Código do status do processo. 0 para OK.
     */
	public boolean delete(){
		return (login != null ? delete(login) : false);
	}
	
    /**
     * Deleta um usuário<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário que será removido<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean true se o usuário for removido com sucesso.
     */
	public boolean delete(String login_par) {
		int status = 999;
		int methodId = 999;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/user/delete/" + login_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do deleteUser
				JSONObject dUsr_json;
				try {
					dUsr_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					lastError = -999;
					return false;
				}
				status = Integer.valueOf(Long.toString((Long)dUsr_json.get("status")));
				if (status != 0){
					//grava o status de erro
					lastError = status;
				}
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = -999;
			return false;
		} 
		return (status == 0 ? true : false);
	}
	
	
	
	
	
    /**
     * Verifica se o usuário existe<br/>
     * Requisitos:<br/>
	 * 		<p>Login padrão definido</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: True para quando o usuário existir
     */
	public boolean check(){
		return (login != null ? check(login) : false);
	}
	
    /**
     * Verifica se o usuário existe<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário a ser consultado
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: True para quando o usuário existir
     */
	public boolean check(String login_par){
		int status = 999;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/user/check/" + login_par;
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do deleteDev
				JSONObject dDev_json;
				try {
					dDev_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					lastError = -999;
					return false;
				}
				//verifica o status
				status = Integer.valueOf(Long.toString((Long)dDev_json.get("status")));
				if (status != 0){
					//grava o status de erro
					lastError = status;
				}
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return (status == 0 ? true : false);
		} 
		return (status == 0 ? true : false);
	}
	
    /**
     * Verifica se o ID do método de autenticação pertence ao usuário<br/>
     * Requisitos:<br/>
	 * 		<p>Login padrão definido</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * int method: ID do método de autenticação que será consultado
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: True se o dispositivo pertencer ao usuário
     */
	public boolean checkDevice(int method_par){
		return (login != null ? checkDevice(login,method_par) : false);
	}
	
	
    /**
     * Verifica se o ID do método de autenticação pertence ao usuário<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String login: Login do usuário que será verificado
	 * int method: ID do método de autenticação que será consultado
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: True se o dispositivo pertencer ao usuário
     */
	public boolean checkDevice(String login_par, int method_par){
		int status = 999;
		int methodId = 999;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + super.path + "/manage/method/check/" + login_par + "/" + String.valueOf(method_par);
		URL url;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url usando o basichttpauth
			HttpGet httpget = new HttpGet(urlString);
			HttpResponse response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response.getEntity();
				//instancia o json recebido do deleteDev
				JSONObject dDev_json;
				try {
					dDev_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					// console.log(balbla);
					e.printStackTrace();
					lastError = -999;
					return false;
				}
				//verifica o status
				status = Integer.valueOf(Long.toString((Long)dDev_json.get("status")));
				if (status != 0){
					//grava o status de erro
					lastError = status;
				}
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return (status == 0 ? true : false);
		} 
		return (status == 0 ? true : false);
	}
		
}
