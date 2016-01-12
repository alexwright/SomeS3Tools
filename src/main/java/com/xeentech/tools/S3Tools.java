package com.xeentech.tools;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

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

public class S3Tools {
    public static AmazonS3Client client;

    public static void main(String [ ] args) {
        Options opts = new Options();
        opts.addOption("a", true, "The rule name to use");
        opts.addOption("b", true, "The bucket name to work on");
        opts.addOption("o", true, "Comma separated list of Origins (use with -b)");
        opts.addOption("h", true, "Comma separated list of Headers (use with -b)");
        opts.addOption("m", true, "Comma separated list of Methods (use with -b)");
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
            setBucketConfig(cmd.getOptionValue("b"), cmd);
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

    private static void setBucketConfig(String bucketName, CommandLine cmd) {
        BucketCrossOriginConfiguration config;
        config = client.getBucketCrossOriginConfiguration(bucketName);

        if (config == null) {
            config = new BucketCrossOriginConfiguration();
        }
        List<CORSRule> rules = config.getRules();
        if (rules == null) {
            System.out.println("No rules at all");
            rules = new ArrayList<CORSRule>();
        }
        else {
            System.out.format("\nConfiguration has %s rules:\n", config.getRules().size());
            for (CORSRule rule : rules) {
                System.out.format("  %s\n", rule.getId());
                System.out.format("  * Allowed Headers: %s\n", rule.getAllowedHeaders().toString());
                System.out.format("  * Allowed Methods: %s\n", rule.getAllowedMethods().toString());
                System.out.format("  * Allowed Origins: %s\n", rule.getAllowedOrigins().toString());
            }
        }

        if (!cmd.hasOption("o") || !cmd.hasOption("m")) {
            System.out.println("No Origins (-o) or Methods (-m) given");
            CORSRule.AllowedMethods.values();
        }
        else {
            List<CORSRule.AllowedMethods> methods = new ArrayList<CORSRule.AllowedMethods>();
            for (String sMethod : (cmd.getOptionValue("m").split(","))) {
                CORSRule.AllowedMethods method = CORSRule.AllowedMethods.fromValue(sMethod);
                methods.add(method);
            }
            List<String> origins = new ArrayList<String>();
            for (String origin : cmd.getOptionValue("o").split(",")) {
                origins.add(origin);
            }
            List<String> headers = new ArrayList<String>();
            for (String header : cmd.getOptionValue("h").split(",")) {
                headers.add(header);
            }
            System.out.println("Headers: " + headers.toString());
            System.out.println("Methods: " + methods.toString());
            System.out.println("Origins: " + origins.toString());

            // Default to "rule1" as before
            String ruleName = "rule1";
            if (cmd.hasOption("a")) {
                ruleName = cmd.getOptionValue("a");
            }

            CORSRule rule = new CORSRule()
                .withId(ruleName)
                .withAllowedHeaders(headers)
                .withAllowedMethods(methods)
                .withAllowedOrigins(origins);

            ArrayList<CORSRule> newRules = new ArrayList<CORSRule>();
            if (cmd.hasOption("a")) {
                newRules.addAll(rules);
            }
            newRules.add(rule);

            config = new BucketCrossOriginConfiguration();
            config.setRules(newRules);
            client.setBucketCrossOriginConfiguration(bucketName, config);
        }
    }
}
