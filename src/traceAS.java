import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class traceAS {
    private final static Pattern ipReg = Pattern.compile("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}");
    private final static Pattern asReg = Pattern.compile("[Oo]riginA?S?: *([\\d\\w]+?)\n");
    private final static Pattern countryReg = Pattern.compile("[Cc]ountry: *([\\w]+?)\n");
    private final static Pattern providerReg = Pattern.compile("mnt-by: *([\\w\\d-]+?)\n");

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("For usage type \"java /path/to/script [name or ip]\"");
            return;
        }
        var intIter = Stream.iterate(1, l -> l + 1).iterator();
        new Scanner(getIpTracert(args[0]))
                .findAll(ipReg)
                .forEach(x -> {
                    try {
                        printTableFrom(getInformationFrom(x.group()), intIter.next());
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    private static void printTableFrom(IpInfo info, int number){
        if(number < 2)
            System.out.printf("%5s %17s %10s %10s %20s%n", "#", "IP", "AS", "Country", "Provider");
        System.out.printf("%5s %17s %10s %10s %20s%n", number, info.ip, info.as, info.country, info.provider);

    }
    private static IpInfo getInformationFrom(String ip) throws URISyntaxException, IOException, InterruptedException {
        var res = new IpInfo(ip, "", "", "");
        if(isGreyIp(ip)){
            return res;
        }
        var url = String.format("https://www.nic.ru/whois/?searchWord=%s", ip);
        var httpClient = HttpClient.newHttpClient();
        var httpRequest = HttpRequest.newBuilder()
                .uri(new URI(url))
                .GET()
                .build();
        var httpResponse = httpClient .send(httpRequest, HttpResponse.BodyHandlers.ofString());
        var sc = new Scanner(httpResponse.body());
        res.country = sc.findAll(countryReg).findFirst().map(x -> x.group(1)).orElse("");
        sc = new Scanner(httpResponse.body());
        res.provider = sc.findAll(providerReg).findFirst().map(x -> x.group(1)).orElse("");
        sc = new Scanner(httpResponse.body());
        res.as = sc.findAll(asReg).findFirst().map(x -> x.group(1)).orElse("");

        return res;
    }

    private static boolean isGreyIp(String ip) {
        return ip.startsWith("192.168.")
                || ip.startsWith("10.")
                || (ip.startsWith("172.")
                    && 15 < Integer.parseInt(ip.split("\\.")[1])
                    && 32 > Integer.parseInt(ip.split("\\.")[1]));
    }

    private static Reader getIpTracert(String name) {
        Process traceRt = null;
        try {
            if(System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win")){
                traceRt = Runtime.getRuntime().exec(String.format("tracert %s", name));
            } else {
                traceRt = Runtime.getRuntime().exec(String.format("traceroute %s", name));
            }
        } catch (IOException e) {
            System.out.println("Please be sure traceroute has been installed");
            return new StringReader("");
        }
        if(new Scanner(traceRt.getInputStream())
                .nextLine().toLowerCase(Locale.ROOT)
                .contains("unable to resolve target system name")){
            System.out.println("Check Internet connection or input correctness");
        }
        return new InputStreamReader(traceRt.getInputStream());
    }

    private static class IpInfo{
        public String ip;
        public String as;
        public String country;
        public String provider;

        public IpInfo(String ip, String as, String country, String provider) {
            this.ip = ip;
            this.as = as;
            this.country = country;
            this.provider = provider;
        }
    }
}