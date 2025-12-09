package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ExpenseJson {
    //? Why "Gson"? We went with this because storing in JSON is easier but a pain to deal with in Java. Gson is a library that makes it easier to serialize and deserialize JSON in Java. 
    // Thanks to some documentations, we were able to get it up and running! :D
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
            .create();

    public static String toJson(Expense expense) {
        return gson.toJson(expense);
    }

    public static Expense fromJson(String json) {
        return gson.fromJson(json, Expense.class);
    }

    // And this too! 🙏
    private static class LocalDateAdapter extends TypeAdapter<LocalDate> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        @Override
        public void write(JsonWriter out, LocalDate value) throws IOException {
            out.value(value.format(formatter));
        }

        @Override
        public LocalDate read(JsonReader in) throws IOException {
            return LocalDate.parse(in.nextString(), formatter);
        }
    }
}

