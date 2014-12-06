package com.xeentech.sdt;

import java.util.List;
import java.util.Arrays;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
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
        opts.addOption("p", true, "The AWS profile to use when loading credentials");

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;
        try {
            cmd = parser.parse(opts, args);
        }
        catch (ParseException e) {
            System.out.println("Unable to parse cmd line args.");
            return;
        }

        String bucketName = cmd.getOptionValue("b");
        if (bucketName == null) {
            System.err.println("Please supply a bucket name with the -b option");
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
