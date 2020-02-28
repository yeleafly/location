package cn.ubuer.location;

import org.apache.commons.lang3.text.StrTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Pattern;


@RestController
@RequestMapping("location")
public class LocationController {
    private static final Logger log = LoggerFactory.getLogger(LocationController.class);

    @RequestMapping(value = "log", method = {RequestMethod.GET})
    public String check(HttpServletRequest request, @RequestBody Map map
    ) {
        String ip = getIpFromRequest(request);
        log.info("ip:{},ua:{},requestMap:{}", ip, request.getHeader("User-Agent"), map);
        return "ok";
    }


    public static final String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    public static final Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");

    public static String longToIpV4(long longIp) {
        int octet3 = (int) ((longIp >> 24) % 256);
        int octet2 = (int) ((longIp >> 16) % 256);
        int octet1 = (int) ((longIp >> 8) % 256);
        int octet0 = (int) ((longIp) % 256);
        return octet3 + "." + octet2 + "." + octet1 + "." + octet0;
    }

    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16)
                + (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }

    public static boolean isIPv4Private(String ip) {
        long longIp = ipV4ToLong(ip);
        return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255"))
                || (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255"))
                || longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
    }

    public static boolean isIPv4Valid(String ip) {
        return pattern.matcher(ip).matches();
    }

    public static String getIpFromRequest(HttpServletRequest request) {
        String ip = "null";
        try {
            boolean found = false;
            if ((ip = request.getHeader("x-forwarded-for")) != null) {
                StrTokenizer tokenizer = new StrTokenizer(ip, ",");
                while (tokenizer.hasNext()) {
                    ip = tokenizer.nextToken().trim();
                    if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                ip = request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return ip;
    }

}
