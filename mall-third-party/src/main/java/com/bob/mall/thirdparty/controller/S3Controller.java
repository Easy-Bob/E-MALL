package com.bob.mall.thirdparty.controller;

import com.bob.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/third")
public class S3Controller {

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKeyId;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    @GetMapping("/s3/policy")
    public R getS3Policy() {
        Map<String, String> respMap = new HashMap<>();
        try {
            // 当前日期目录
            String dir = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/";

            // 过期时间 5 分钟
            Date now = new Date();
            Date expiration = new Date(now.getTime() + 300 * 1000);
            String iso8601Date = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'").format(now);
            String shortDate = new SimpleDateFormat("yyyyMMdd").format(now);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            String policyJson = "{\n" +
                    "  \"expiration\": \"" + sdf.format(expiration) + "\",\n" +
                    "  \"conditions\": [\n" +
                    "    {\"bucket\": \"" + bucket + "\"},\n" +
                    "    [\"starts-with\", \"$key\", \"" + dir + "\"],\n" +
                    "    {\"acl\": \"public-read\"},\n" +
                    "    {\"x-amz-algorithm\": \"AWS4-HMAC-SHA256\"},\n" +
                    "    [\"starts-with\", \"$x-amz-credential\", \"\"],\n" +
                    "    [\"starts-with\", \"$x-amz-date\", \"\"]\n" +
                    "  ]\n" +
                    "}";


            String policyBase64 = Base64.getEncoder().encodeToString(policyJson.getBytes(StandardCharsets.UTF_8));

            // AWS4 签名流程
            byte[] signingKey = getSignatureKey(secretKey, shortDate, region, "s3");
            String signature = toHex(HmacSHA256(policyBase64.getBytes(StandardCharsets.UTF_8), signingKey));

            String host = "https://" + bucket + ".s3." + region + ".amazonaws.com";

            respMap.put("policy", policyBase64);
            respMap.put("signature", signature);
            respMap.put("credential", accessKeyId + "/" + shortDate + "/" + region + "/s3/aws4_request");
            respMap.put("algorithm", "AWS4-HMAC-SHA256");
            respMap.put("date", iso8601Date);
            respMap.put("dir", dir);
            respMap.put("host", host);

        } catch (Exception e) {
            e.printStackTrace();
            return R.error("生成 S3 Policy 失败");
        }
        return R.ok().put("data", respMap);
    }

    private static byte[] HmacSHA256(byte[] data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data);
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = HmacSHA256(dateStamp.getBytes(StandardCharsets.UTF_8), kSecret);
        byte[] kRegion = HmacSHA256(regionName.getBytes(StandardCharsets.UTF_8), kDate);
        byte[] kService = HmacSHA256(serviceName.getBytes(StandardCharsets.UTF_8), kRegion);
        return HmacSHA256("aws4_request".getBytes(StandardCharsets.UTF_8), kService);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
}
