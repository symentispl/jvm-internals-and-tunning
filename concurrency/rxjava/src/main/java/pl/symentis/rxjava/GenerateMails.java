package pl.symentis.rxjava;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import com.google.common.base.Charsets;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.io.Files;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class GenerateMails {

	private static Observable o2;

	public static void main(String[] args) throws IOException {

		Supplier<Template> template = Suppliers.memoize(() -> template());

		CSVParser parse = CSVParser.parse(new File("MOCK_DATA.csv"), Charsets.UTF_8,
				CSVFormat.newFormat(','));		

		Observable
				.from(parse)
				.observeOn(Schedulers.io())
				.subscribeOn(Schedulers.io())
				.filter(isEmailValid())
				.map(generateMail(template.get()))
				.subscribe(t -> {
					System.out.println(Thread.currentThread());
					try {
						Files.write(t, java.nio.file.Files.createTempFile("mail", "").toFile(), Charsets.UTF_8);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
		System.in.read();
	}

	private static Func1<? super CSVRecord, Boolean> isEmailValid() {
		return t -> {
			String string = t.get(3);
			return Pattern
					.matches(
							"^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$",
							string);
		};
	}

	public static Func1<Object, String> generateMail(Template template) {

		return record -> {
			StringWriter writer = new StringWriter();
			try {
				template.process(record, writer);
			} catch (TemplateException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return writer.toString();
		};
		
	}

	private static Template template() {
		Configuration cfg = new Configuration(Configuration.VERSION_2_3_23);

		cfg.setClassForTemplateLoading(GenerateMails.class, "/templates");

		cfg.setDefaultEncoding("UTF-8");
		cfg.setLocale(Locale.US);
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

		try {
			return cfg.getTemplate("email.ftl");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
