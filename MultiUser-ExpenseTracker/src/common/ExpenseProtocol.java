package common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseProtocol {
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;

    public static Expense parseServerMessage(String message) {
        String[] parts = message.split("\\|");
        if (parts.length < 5) {  return null; }

        double amount = Double.parseDouble(parts[2]);
        String category = parts[3];
        LocalDate date = LocalDate.parse(parts[4], dateFormatter);
        String note = parts.length > 5 ? parts[5] : "";
        
        return new Expense(amount, category, date, note);
    }

    public static String toServerMessage(Expense expense, String username) {
        return String.format("ADD_EXPENSE|%s|%.2f|%s|%s|%s",
                username, expense.getAmount(), expense.getCategory(),
                expense.getDate().format(dateFormatter), expense.getNote());
    }
}

