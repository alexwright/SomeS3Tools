package com.xeentech.sdt;

import java.util.List;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;

public class AfTools {
    public static AmazonS3Client client;

    public static void main(String [ ] args) {
        client = new AmazonS3Client(new ProfileCredentialsProvider());

        List<Bucket> buckets = client.listBuckets();
        for (Bucket b : buckets) {
            System.out.println("Bucket: " + b.getName());
        }
    }
}
