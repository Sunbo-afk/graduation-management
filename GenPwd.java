import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

public class GenPwd {
    public static void main(String[] args) throws Exception {
        // Manually use BCrypt from the Maven repo jars
        String repo = System.getProperty("user.home") + "/.m2/repository";
        URL[] urls = {
            new File(repo + "/org/springframework/security/spring-security-crypto/5.7.11/spring-security-crypto-5.7.11.jar").toURI().toURL(),
            new File(repo + "/org/springframework/spring-core/5.3.31/spring-core-5.3.31.jar").toURI().toURL(),
            new File(repo + "/org/springframework/spring-jcl/5.3.31/spring-jcl-5.3.31.jar").toURI().toURL(),
            new File(repo + "/commons-logging/commons-logging/1.2/commons-logging-1.2.jar").toURI().toURL(),
        };
        URLClassLoader cl = new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
        Class<?> encoderClass = cl.loadClass("org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder");
        Object encoder = encoderClass.getDeclaredConstructor().newInstance();
        String hash = (String) encoderClass.getMethod("encode", CharSequence.class).invoke(encoder, "123456");
        System.out.println(hash);
    }
}
