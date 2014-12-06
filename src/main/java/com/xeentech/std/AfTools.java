package com.xeentech.sdt;

import java.util.List;
import java.util.Arrays;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.Region;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class AfTools {
    public static AmazonS3Client client;

    public static void main(String [ ] args) {
        Options opts = new Options();
        opts.addOption("b", true, "The bucket name to work on");
        opts.addOption("c", true, "Create a new bucket");
        opts.addOption("p", true, "The AWS profile to use when loading credentials");
        opts.addOption("l", false, "List buckets on the account");
        opts.addOption("r", true, "S3 Region to use");
        opts.addOption("listregions", false, "Get a list of regions");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(opts, args);
        }
        catch (ParseException e) {
            System.out.println("Unable to parse cmd line args.");
            return;
        }

        ProfileCredentialsProvider creds;
        if (cmd.hasOption("p")) {
            creds = new ProfileCredentialsProvider(cmd.getOptionValue("p"));
        }
        else {
            creds = new ProfileCredentialsProvider();
        }
        client = new AmazonS3Client(creds);

        // Create a bucket
        if (cmd.hasOption("c")) {
            String bucketName = cmd.getOptionValue("c");
            if (cmd.hasOption("r")) {
                try {
                    Region region = Region.fromValue(cmd.getOptionValue("r"));
                    client.createBucket(bucketName, region);
                }
                catch (java.lang.IllegalArgumentException e) {
                    System.err.format("Unable to find region '%s'", cmd.getOptionValue("r"));
                }
            }
            else {
                client.createBucket(bucketName);
            }
        }

        // List Buckets
        if (cmd.hasOption("l")) {
            listBuckets();
        }

        // Bucket Config
        if (cmd.hasOption("b")) {
            setBucketConfig(cmd.getOptionValue("b"));
        }

        // List Regions
        if (cmd.hasOption("listregions")) {
            for (Region r : Region.values()) {
                System.out.println(r.toString());
            }
        }
    }

    private static void listBuckets() {
        List<Bucket> buckets = client.listBuckets();
        System.out.format("%d buckets found:\n", buckets.size());
        for (Bucket b : buckets) {
            System.out.println("Bucket: " + b.getName());
        }
    }

    private static void setBucketConfig(String bucketName) {
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
