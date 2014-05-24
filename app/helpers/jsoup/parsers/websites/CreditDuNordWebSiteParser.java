package helpers.jsoup.parsers.websites;

import helpers.jsoup.JsoupConnection;
import helpers.jsoup.parsers.accounts.AccountParserResult;
import helpers.jsoup.parsers.accounts.CreditDuNordAccountParser;
import helpers.jsoup.parsers.accounts.IAccountParser;
import helpers.jsoup.parsers.transactions.CreditDuNordTransactionParser;
import helpers.jsoup.parsers.transactions.ITransactionParser;
import helpers.jsoup.parsers.transactions.TransactionParserResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Account;

import org.joda.time.DateTime;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public class CreditDuNordWebSiteParser implements IWebSiteParser {

	private String login;

	private String password;

	private Map<String, String> cookies;

	public CreditDuNordWebSiteParser(String login, String password) {
		this.login = login;
		this.password = password;
	}

	@Override
	public List<AccountParserResult> retrieveAccounts() {
		List<AccountParserResult> results = null;

		if (cookies == null) {
			authenticate();
		}

		String url = "https://www.credit-du-nord.fr/vos-comptes/particuliers";

		Response response = JsoupConnection.get(url, cookies);
		if (response != null) {
			try {
				Document document = response.parse();

				IAccountParser parser = new CreditDuNordAccountParser();
				results = parser.parse(document);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return results;
	}

	@Override
	public List<TransactionParserResult> retrieveTransactions(Account account) {
		List<TransactionParserResult> results = null;

		if (cookies == null) {
			authenticate();
		}

		String url1 = "https://www.credit-du-nord.fr/vos-comptes/particuliers/V1_transactional_portal_page_26";
		Response response1 = JsoupConnection.get(url1, cookies);
		if (response1 != null) {
			DateTime now = DateTime.now();

			String url2 = "https://www.credit-du-nord.fr/vos-comptes/IPT/appmanager/transac/particuliers?_cdnRemUrl=%2FtransacClippe%2FTEL_2.asp&_cdnPtlKey=MTIyNDA%3D";
			Map<String, String> data2 = new HashMap<>();
			{
				data2.put("urlPROXYTCH", "/vos-comptes/IPT/appmanager/transac/particuliers");
				data2.put("box1", "on");
				data2.put("banque1", account.bank.number);
				data2.put("agence1", account.agency);
				data2.put("classement1", account.rank);
				data2.put("serie1", account.series);
				data2.put("sscompte1", account.subAccount);
				data2.put("devise1", "EUR");
				data2.put("deviseCCB1", "0131");
				data2.put("nbCompte", Integer.toString(account.bank.accounts.size()));
				data2.put("ChoixDate", "Autres");
				data2.put("JourDebut", "01");
				data2.put("MoisDebut", "01");
				data2.put("AnDebut", "2000");
				data2.put("JourFin", Integer.toString(now.getDayOfMonth()));
				data2.put("MoisFin", Integer.toString(now.getMonthOfYear()));
				data2.put("AnFin", Integer.toString(now.getYear()));
				data2.put("logiciel", "TXT");
			}

			Response response2 = JsoupConnection.post(url2, cookies, data2);
			if (response2 != null) {
				String url3 = "https://www.credit-du-nord.fr/vos-comptes/IPT/cdnProxyResource/transacClippe/TCH_Envoi_1.asp";

				Response response3 = JsoupConnection.get(url3, cookies);
				if (response3 != null) {
					byte[] bytes = response3.bodyAsBytes();
					ByteArrayInputStream input = new ByteArrayInputStream(bytes);

					ITransactionParser parser = new CreditDuNordTransactionParser();
					results = parser.parse(input);
				}
			}
		}

		return results;
	}

	private void authenticate() {
		String url = "https://www.credit-du-nord.fr/saga/authentification";
		Map<String, String> data = new HashMap<>();
		{
			data.put("bank", "credit-du-nord");
			data.put("username", login);
			data.put("password", password);
		}

		Response response = JsoupConnection.post(url, null, data);
		if (response != null) {
			cookies = response.cookies();
		}
	}
}