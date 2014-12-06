package com.xeentech.sdt;

import java.util.List;
import java.util.Arrays;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class AfTools {
    public static AmazonS3Client client;
    public static String bucketName = "somename";

    public static void main(String [ ] args) {
        client = new AmazonS3Client(new ProfileCredentialsProvider());

        List<Bucket> buckets = client.listBuckets();
        for (Bucket b : buckets) {
            System.out.println("Bucket: " + b.getName());
        }

        CORSRule rule = new CORSRule()
            .withId("rule1")
            .withAllowedMethods(Arrays.asList(new CORSRule.AllowedMethods[] {
                CORSRule.AllowedMethods.PUT,
                CORSRule.AllowedMethods.POST,
            }))
            .withAllowedOrigins(Arrays.asList(new String[] {
                "http://web01.salesfollowup123.com/",
                "http://afnew.mimas.xeentech.com/",
            }));

        BucketCrossOriginConfiguration config;
        config = client.getBucketCrossOriginConfiguration(bucketName);

        if (config == null) {
            config = new BucketCrossOriginConfiguration();
        }
        List<CORSRule> rules = config.getRules();
        if (rules == null) {
            System.out.println("No rules at all");
        }
        else {
            System.out.format("\nConfiguration has %s rules:\n", config.getRules().size());
        }
    }
}
