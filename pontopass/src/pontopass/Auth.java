package pontopass;
/****************************************************************************                                                                          
*                     PontoSec Segurança da Informação                      *
*                     														*
*  @Product			Pontopass												*
*  @ID				PPAPIJ01	                                        	*
*  @Projeto			API: Java												*
*  @Program			Interface para conexão e autenticação no Pontopass		*
*  @Author  	    Guilherme Cesar Leite                               	*
*  @version			17/05/2013					                        	*
*                                                                           *
*  Auth.java																*
*  Descrição:																*
*  Esta classe é uma API que implementa todas as funções necessárias para 	*
*  acessar e utilizar o Pontopass. 											*
*																			*
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
*	1.0.1	18/06/2013		Guilherme Leite	 ---  Tratamento de Exceções	*
*	1.1		27/02/2014		Guilherme Leite	 001  Validação Whatsapp		*
*																			*
****************************************************************************/
/*
 * Imports
 */

//Java.*
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
//Org.apache.http.*
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
//Org.Json.Simple.*
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import javax.servlet.http.*;


public class Auth {
	//private variables
	public String app_user, app_pass;
	public String path = "api.pontopass.com";
	public int lastError = 9999;
	protected String usrLogin;
	private String session,ip,user_agent;
	protected int save,integrationType,selectedMethodType = 0;
	private boolean init = false, list = false, ask = false, validate = false;
	private JSONArray methodList;
	public Auth(){
	}
	
	public Auth(String user, String pass){
		app_user = user;
		app_pass = pass;
	}
	
	/**
     * Define os dados de acesso à API. <br/>
     * Parâmetros:<br/>
     * Entrada----------------------------------------------------------------------------<br/>
     * String apiUser -> ID para acesso a API<br/>
     * String apiPassword -> Chave de acesso à API
     */
	public void setCredentials(String apiUser, String apiPassword){
		app_user = apiUser;
		app_pass = apiPassword;
	}
	
	
    /**
     * ---- Método indisponível nesta versão -----
     * Inicializa uma nova session para o usuário com Ip e user agent determinados automaticamente.<br/>
     * Parâmetros:<br/>
     * Entrada----------------------------------------------------------------------------<br/>
     * String login -> Login do usuário<br/>
     * booelan save_session -> True: Grava a session no database, False: não mantém a sessão(padrão)<br/>
     * booelan use_widget -> True: Utiliza Widget, False: Utilização via SDK(padrão)<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	private boolean init(String login, boolean save_session, boolean use_widget){
		String ip_loc, user_agent_loc;
		//String request = ((HttpServletRequest) getExternalContext().getRequest()).getHeader("x-forwarded-for");
		ip_loc = "192.168.0.1";
		user_agent_loc = "test";
		return init(login,save_session,use_widget,ip_loc,user_agent_loc);
	}
	
	/**
     * Define o caminho para a API. Por padrão, é utilizada o caminho genérico.<br/>
     * Parâmetros:<br/>
     * Entrada----------------------------------------------------------------------------<br/>
     * String newPath -> Path para a API, fornecido pela Pontosec<br/>
     */
	public void setPath(String newPath){
		path = newPath;
	}


    /**
     * Inicializa uma nova session para o usuário com Ip e user agent enviados manualmente.<br/>
     * Parâmetros:<br/>
     * Entrada----------------------------------------------------------------------------<br/>
     * String login: Login do usuário<br/>
     * booelan save_session -> True: Grava a session no database, False: não mantém a sessão(padrão)<br/>
     * booelan use_widget -> True: Utiliza Widget, False: Utilização via SDK(padrão)<br/>
     * String ip: ip do usuário.<br/>
     * String user_agent: user agent do usuário. <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	public boolean init(String login, boolean save_session, boolean use_widget,String ip_par, String user_agent_par){
		int status = 999;
		save = (save_session == true ? 1 : 0);
		usrLogin = login;
		integrationType = (use_widget == true ? 1 : 0);
		ip = ip_par;
		user_agent = user_agent_par;
		//prepara os dados do BasicHttpAuthentication
		String auth = app_user + ":" + app_pass + "@";
		//cria a URL para acesso do método
		String urlString = "https://" + auth + path + "/init/" + login + "/" + String.valueOf(integrationType) + "/" +
								String.valueOf(save)+ "/" + ip + "/" + URLEncoder.encode(user_agent);
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
				//instancia o json recebido do init
				JSONObject init_json = new JSONObject();
				try {
					init_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException e) {
					//console.log("falha em blablalba");
					e.printStackTrace();
					lastError = -999;
					return false;
				}
				//verifica o status
				status = Integer.valueOf(Long.toString((Long)init_json.get("status")));
				if (status == 0){
					//grava o numero da session
					init = true;
					session = (String) init_json.get("session");
				}
				else lastError = status;
			}	
			else lastError = status;
		} catch (IOException e) {
			lastError = status;
			return (status == 0 ? true : false);
		} 
		return (status == 0 ? true : false);
	}
	
    /**
     * Lista os dispositivos do usuario da session inicializada.<br/>
     * Requisitos:<br/>
	 * 		<p>Session válida existente</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String oldSesssion: Número da sessão existente<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * NULL caso não obtenha os dados corretamente ou </br>
     * JSONArray contendo os métodos de login do usuário. <br/>Campos:<br/>
     * {"description":"descrição",<br/>
     *  "id":"Id do método",<br/>
     *  "number":"número de telefone",<br/>
     *  "token_type":"tipo de método"}
     */
	public JSONArray listMethods(String oldSession){
		session = oldSession;
		init = true;
		return listMethods();
	}
	
	
    /**
     * Lista os dispositivos do usuario da session inicializada.<br/>
     * Requisitos:<br/>
	 * 		<p>Session válida inicializada</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * NULL caso não obtenha os dados corretamente ou </br>
     * JSONArray contendo os métodos de login do usuário. <br/>Campos:<br/>
     * {"description":"descrição",<br/>
     *  "id":"Id do método",<br/>
     *  "number":"número de telefone",<br/>
     *  "token_type":"tipo de método"}
     */
	public JSONArray listMethods(){
		if (init){
			int status = 0;
			//cria a URL para acesso do método
			String urlString = "";
			urlString = (integrationType == 1 ? ("https://" + path + "/list/" + session) : ("https://" + path + "/list/" + session + "/" + ip +"/" + user_agent));
			URL url;
			try {
				url = new URL(urlString);
				HttpClient httpclient = new DefaultHttpClient();
				//cria a conexão Http com a url
				HttpGet httpget = new HttpGet(urlString);
				HttpResponse response = httpclient.execute(httpget);
				status = response.getStatusLine().getStatusCode();
				if (status == 200){	
					HttpEntity entity = response.getEntity();
					//instancia o json recebido do list
					JSONObject list_json;
					JSONArray device_array = new JSONArray();
					try {
						list_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
						int list_status = Integer.valueOf(Long.toString((Long)list_json.get("status")));
						if (list_status == 0)
							device_array =  (JSONArray)new JSONParser().parse(list_json.get("methods").toString());
						else{
							lastError = list_status;
							return null;
						}
					} catch (ParseException
							| org.json.simple.parser.ParseException | NullPointerException e) {
						e.printStackTrace();
						//device_array =  (JSONArray)new JSONParser().parse(list_json.get("status").toString());
						return null;
						
					}
					//obtem os dados do device
					if (device_array.size() > 0){
						list = true;
						methodList = device_array;
					}
					return device_array;
				}
				else lastError = status;
			} catch (IOException e) {
				//console.log.blablabla
				return null;
			}
		}
		return null; 
	}
	
    /**
     * Solicita a autenticação com o método desejado<br/>
     * Requisitos:<br/>
	 * 		<p>Session válida inicializada<br/>Métodos para autenticação listados</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * Int methodID: ID do método, obtido no array recebido do List<br/> 
	 * Automaticamente será determinado o tipo de autenticação desejada e gerado o processo correspondente.
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	public boolean ask(int methodID){
		JSONObject tempDevice = new JSONObject();
		int selectedMethodID; 
		for(int i = 0; i < methodList.size(); i++){
			tempDevice = (JSONObject) methodList.get(i);
			selectedMethodID = Integer.valueOf(Long.toString((Long)tempDevice.get("id")));
			if (selectedMethodID == methodID)
				return ask(tempDevice);
		}
		return false;
	}
	
    /**
     * Solicita a autenticação com o método desejado<br/>
     * Requisitos:<br/>
	 * 		<p>Session válida inicializada<br/>Métodos para autenticação listados</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * JSONObject method: Objeto JSON do método desejado, incluído no JSONArray gerado no list.<br/> 
	 * Automaticamente será determinado o tipo de autenticação desejada e gerado o processo correspondente.
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	public boolean ask(JSONObject method){
		int status = 0;
		if (list){
			//cria a URL para acesso do método
			String urlString = "";
			int methodID =  Integer.valueOf(Long.toString((Long)method.get("id")));
			urlString = (integrationType == 1 ? ("https://" + path + "/ask/" + session + String.valueOf(methodID)) : ("https://" + path + "/ask/" + session + "/" + String.valueOf(methodID) + "/" + ip +"/" + user_agent));
			URL url;
			try {
				url = new URL(urlString);
				HttpClient httpclient = new DefaultHttpClient();
				//cria a conexão Http com a url
				HttpGet httpget = new HttpGet(urlString);
				HttpResponse response = httpclient.execute(httpget);
				status = response.getStatusLine().getStatusCode();
				if (status == 200){	
					HttpEntity entity = response.getEntity();
					//instancia o json recebido do ask
					JSONObject ask_json = new JSONObject();
					try {
						ask_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
					} catch (ParseException
							| org.json.simple.parser.ParseException e) {
						//console.log.blablabla
						e.printStackTrace();
						lastError = -999;
						return false;
					}
					//obtem o status do processo
					status = Integer.valueOf(Long.toString((Long)ask_json.get("status")));
					if (status == 0){
						//grava o tipo de método selecionado
						selectedMethodType = Integer.valueOf(Long.toString((Long)method.get("token_type")));
						ask = true;
					}
					else lastError = status;
					return (status == 0 ? true : false);
				}
				else lastError = status;
			} catch (IOException e) {
				lastError = 999;
				return false;
			}
		}
		else return false;
		return (status == 0 ? true : false); 
	}
	
    /**
     * Valida a autenticação desejada<br/>
     * Requisitos:<br/>
	 * 		<p>Autenticação solicitada</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	public boolean validate(){
		return validate("000000");
	}
	
    /**
     * Valida a autenticação desejada<br/>
     * Requisitos:<br/>
	 * 		<p>Autenticação solicitada</p>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * String Code: Código para validação (apenas métodos mobile Token e SMS).<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Boolean: <i>true</i>:OK - <i>false</i>:ERROR
     */
	public boolean validate(String code){
		int status = 999;
		boolean validated = false;
		String urlString = null;
		switch (selectedMethodType){
		case 1: // telefone	
			validated = true;
			break;
		case 2: // sms
			urlString = (integrationType == 1 ? ("https://" + path + "/validate/sms/" + session + "/" + code ) : ("https://" + path + "/validate/sms/" + session + "/" + code + "/" + ip +"/" + user_agent));
			break;
		case 3: // push
			validated = true;
			break;
		case 4: // token
			urlString = (integrationType == 1 ? ("https://" + path + "/validate/token/" + session + "/" + code ) : ("https://" + path + "/validate/token/" + session + "/" + code + "/" + ip +"/" + user_agent));
			break;	
		// 001 - Inicio - Inclusão do método de validação whatsapp 
		case 5: // whatsapp
			validated = true;
			break;
		// 001 - Fim - Inclusão do método de validação whatsapp 
		default:
			return false;
		}
		//realiza a chamada para o validate para os métodos necessários
		if (urlString != null){		
			HttpResponse response_validate;
			try {
				URL validateURL  = new URL(urlString);
				HttpClient httpclient = new DefaultHttpClient();
				//cria a conexão Http com a url
				HttpGet httpget_validate = new HttpGet(urlString);
				
				response_validate = httpclient.execute(httpget_validate);
			} catch (IOException e1 ) {
				e1.printStackTrace();
				return false;
			}
			status = response_validate.getStatusLine().getStatusCode();
			if (status == 200){	
				HttpEntity entity = response_validate.getEntity();
				//instancia o json recebido do validate
				JSONObject valid_json;
				try {
					valid_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
				} catch (ParseException | org.json.simple.parser.ParseException
						| IOException e) {
					e.printStackTrace();
					return false;
				}
				//obtem o status do processo
				status = Integer.valueOf(Long.toString((Long)valid_json.get("status")));
				if (status == 0){
					validated = true;
					status = 999;
				}
				else lastError = status;
			}
			else lastError = status;
		}
		//verfica o status do processo
		if (validated){
			return checkAuth(usrLogin);
		}
		return false;
	}
	
    /**
     * Verifica o status da sessão atual<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * Int statusCode: Código do status do processo
     */
	private boolean checkAuth(String username){
		//prepara os dados do BasicHttpAuthentication
		String auth = "https://" + app_user + ":" + app_pass + "@";
		String urlString = auth + path + "/auth/" + session + "/" + ip +"/" + user_agent;
		URL url;
		HttpResponse response;
		int status;
		try {
			url = new URL(urlString);
			HttpClient httpclient = new DefaultHttpClient();
			//cria a conexão Http com a url
			HttpGet httpget = new HttpGet(urlString);
			response = httpclient.execute(httpget);
			status = response.getStatusLine().getStatusCode();
		} catch(IOException e){
			e.printStackTrace();
			lastError =  -999;
			return false;
		}
		if (status == 200){	
			HttpEntity entity = response.getEntity();
			//instancia o json recebido do auth
			JSONObject auth_json;
			try {
				auth_json = (JSONObject)new JSONParser().parse(EntityUtils.toString(entity));
			} catch (ParseException | org.json.simple.parser.ParseException
					| IOException e) {
				e.printStackTrace();
				lastError = -999;
				return false;
			}
			//obtem o status do processo
			status = Integer.valueOf(Long.toString((Long)auth_json.get("status")));
			if (status == 0){
				String authUser = auth_json.get("user").toString();
				return authUser.equalsIgnoreCase(username);
			}
			else lastError = status;
		}
		else lastError = status;
		lastError = status;
		return false;
	}
	
	
    /**
     * Converte um JSONArray em um ArrayList<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * JSONArray jsArray: Array JSON para conversão<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * ArrayList: Array correspondente ao JSON de entrada 
     */
	private static ArrayList toArrayList(JSONArray jsArray){
		ArrayList<String> list = new ArrayList<String>();     
		if (jsArray != null) { 
		   int len = jsArray.size();
		   for (int i=0;i<len;i++){ 
		    list.add(jsArray.get(i).toString());
		   } 
		} 
		return list;
	}
	
    /**
     *   ----  Indisponível neste versão ----
     * Converte um JSONObject em um ArrayList<br/>
	 * Parâmetros: <br/>
	 * Entrada----------------------------------------------------------------------------<br/>
	 * JSONArray jsArray: Array JSON para conversão<br/>
     * Saída------------------------------------------------------------------------------<br/>
     * ArrayList: Array correspondente ao JSON de entrada 
     */
	/*private static ArrayList toArrayList(JSONObject jsObj){
		ArrayList<String> list = new ArrayList<String>();  
		java.util.Set entries = jsObj.entrySet();
		//entries.
		if (jsObj != null) { 
			int len = jsObj.size();
			for (int i=0;i<len;i++){ 
				System.out.println(jsObj.values().);//String.valueOf(i))
				list.add("1");
				
			} 
		} 
		return list;
	}*/
	
    /**
     * Retorna o numero da session inicializada<br/>
     * Requisitos:<br/>
	 * 		<p>Sessão inicializada</p>
	 * Parâmetros: <br/>
     * Saída------------------------------------------------------------------------------<br/>
     * String: String correspondente a session atual inicializada, ou null
     */
	public String getSession(){
		return session;
	}
	
    /**
     * Elimina a session atual e todos os dados inicializados<br/>
     */
	public void kill(){
		app_user = null; 
		app_pass = null;
		session = null;
		ip = null;		user_agent = null;
		save = 0;
		integrationType = 0;
		selectedMethodType = 0;
		init = false;
		list = false; 
		ask = false; 
		validate = false;
		methodList = null;	
	}
	public String getStatus(){
		switch(lastError){
		case 0:   
			return "Sucesso";
		case 20:   
			return "Erro ao iniciar";
		case 30:   
			return "Chave inválida";
		case 100: 
			return "Sessão criada";
		case 110: 
			return "Novo dispositivo permitido para o usuário";
		case 115:
			return "Sem dispositivo cadastrado, permitir acesso";
		case 150: 
			return "Erro ao gravar session";
		case 151: 
			return "Erro ao gravar usuário - Usuário já existe";
		case 152: 
			return "Erro ao gravar usuário";
		case 153: 
			return "Erro ao deletar usuário";
		case 155: 
			return "IP e/ou User Agent incorreto(s)";
		case 210: 
			return "Erro de gravação no cache";
		case 220: 
			return "Erro de leitura no cache";
		case 310: 
			return "Erro Interno";
		case 320: 
			return "Erro Interno";
		case 400: 
			return "Usuário não encontrado";
		case 405: 
			return "Erro Interno";
		case 410: 
			return "Sessão não encontrada";
		case 411: 
			return "Aplicação não corresponde a session";
		case 413: 
			return "Session inválida";
		case 415: 
			return "Dispositivo não encontrado";
		case 420: 
			return "Método não encontrado";
		case 422: 
			return "Aplicação não encontrada";
		case 425: 
			return "Método não encontrado";
		case 440: 
			return "Erro Interno";
		case 450: 
			return "Erro Interno";
		case 490: 
			return "Sem dispositivo cadastrado, bloquear acesso";
		case 492: 
			return "Telefone invalido";
		case 495: 
			return "Novo método inserido";
		case 510: 
			return "Erro na Ligação";
		case 520: 
			return "Erro no SMS";
		case 530: 
			return  "Erro no mobile Token";
		case 540: 
			return "Erro no Aplicativo Mobile";
		case 600: 
			return "Sem créditos disponíveis";
		case 710: 
			return "Erro no Login";
		case 720: 
			return "IP Inválido";
		case 740: 
			return "IP Bloqueado";
		case 750: 
			return "Bloqueado - IP TOR";
		case 760: 
			return "Bloqueado - IP PROXY";
		case 770: 
			return "Bloqueado - IP Blacklist";
		case 790: 
			return "Acesso não permitido - Conta vencida";
		case 795: 
			return "Acesso não permitido - Limite de usuários cadastrados";
		case 800: 
			return "Aguardando resposta";
		case 810: 
			return "Login bloqueado - SMS";
		case 820: 
			return "Login bloqueado – Mobile Token";
		case 830: 
			return "Login bloqueado – Chamada não atendida";
		case 840: 
			return "Login bloqueado - Push / Telefone";
		case 850:
			return "Login bloqueado - Whatsapp";
		case -999:
			return "Falha ao conectar no Pontopass";
		case 999:
			return "Falha ao conectar no Pontopass";
		case 9999:
			return "Initial";
		default:
			return "Contate o administrador: Status " + String.valueOf(lastError);
		}
	}
}
