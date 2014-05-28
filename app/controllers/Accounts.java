package controllers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jobs.synchronizers.TransactionsSynchronizer;
import models.Account;
import models.transactions.oneoff.OneOffTransaction;
import models.transactions.regular.Category;
import models.transactions.regular.RegularTransaction;

import org.joda.time.DateTime;

public class Accounts extends SuperController {

	public static void index() {
		List<Account> accounts = Account.findAll();

		render(accounts);
	}

	public static void show(Long accountId, Integer year, Integer month) {
		if (accountId == null) {
			index();
		}

		DateTime now = DateTime.now();
		int currentYear = now.getYear();
		int currentMonth = now.getMonthOfYear();

		if (year == null || month == null) {
			year = currentYear;
			month = currentMonth;
		}

		// Dates
		List<DateTime> dates = new LinkedList<>();
		for (int y = currentYear - 3; y <= currentYear; y++) {
			for (int m = 1; m <= 12; m++) {
				dates.add(new DateTime(y, m, 1, 0, 0));
			}
		}

		// Categories
		List<Category> categories = Category.findAll();

		// Regular transactions
		Map<Category, List<RegularTransaction>> regularTransactions = new HashMap<>();
		for (Category category : categories) {
			regularTransactions.put(category, RegularTransaction.findByAccountIdAndCategoryIdAndYearAndMonth(accountId, category.id, year, month));
		}

		// One-off transactions
		List<OneOffTransaction> oneOffTransactions = OneOffTransaction.findByAccountIdAndYearAndMonth(accountId, year, month);

		render(accountId, year, month, currentYear, currentMonth, dates, categories, regularTransactions, oneOffTransactions);
	}

	public static void synchronize(long accountId) {
		new TransactionsSynchronizer(accountId).now();

		index();
	}
}
