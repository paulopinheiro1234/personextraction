package raw.crawling;

public class UrlUtils {

    public static String getDominioPrincipal(String url) {
        try {
            String host = new java.net.URL(url).getHost(); // ex: www.ipiaget.org
            host = host.replaceFirst("^www\\.", ""); // remove www.
            int lastDot = host.lastIndexOf('.');
            if (lastDot != -1) {
                return host.substring(0, lastDot); // ex: ipiaget
            }
            return host;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDominioBase(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getProtocol() + "://" + u.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getHost(String url) {
        try {
            java.net.URL u = new java.net.URL(url);
            return u.getHost();
        } catch (Exception e) {
            return "";
        }
    }

    // ✅ Novo método que compara se duas URLs pertencem ao mesmo domínio base (ex:
    // ipiaget)
    public static boolean pertenceAoMesmoDominio(String urlPrincipal, String urlComparar) {
        try {
            String hostPrincipal = new java.net.URL(urlPrincipal).getHost().replaceFirst("^www\\.", "");
            String hostComparar = new java.net.URL(urlComparar).getHost().replaceFirst("^www\\.", "");

            String nomeBasePrincipal = hostPrincipal.split("\\.")[0];
            String nomeBaseComparar = hostComparar.split("\\.")[0];

            return nomeBasePrincipal.equalsIgnoreCase(nomeBaseComparar);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean saoDominiosRelacionados(String url1, String url2) {
        String d1 = getDominioPrincipal(url1).replaceAll("\\W", "");
        String d2 = getDominioPrincipal(url2).replaceAll("\\W", "");
        return d1.contains(d2) || d2.contains(d1);
    }
}
