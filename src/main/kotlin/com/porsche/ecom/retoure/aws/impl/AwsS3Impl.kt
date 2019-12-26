package com.porsche.ecom.retoure.aws.impl

import com.porsche.ecom.retoure.aws.AwsS3
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.internal.util.EC2MetadataUtils
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception

/** Wrapper for AWS S3 classes. */
@Component
class AwsS3Impl(private val environment: Environment) : AwsS3 {

    private val region: Region = try {
        Region.of(EC2MetadataUtils.getEC2InstanceRegion())
    } catch (e: SdkClientException) {
        Region.EU_WEST_1
    }

    companion object {
        private const val BUCKETNAME_ENV_VAR = "S3BucketName"
    }

    /**
     * Returns a file from the AWS S3 Bucket.
     *
     * @param filename The name (key) of the file on the AWS S3 Bucket
     * @return The bytes of the file from the AWS S3 Bucket
     */
    override fun getFile(filename: String): ByteArray =
        try {
            getS3Client().use {
                return it.getObjectAsBytes(
                    GetObjectRequest.builder().bucket(environment.getProperty(BUCKETNAME_ENV_VAR)).key(filename).build()
                ).asByteArray() ?: byteArrayOf()
            }
        } catch (e: S3Exception) {
            println("A S3Exception occurred: '${e.message}'")
            e.printStackTrace()
            byteArrayOf()
        }

    /**
     * Returns the initialized [S3Client].
     *
     * @return the initialized [S3Client]
     */
    private fun getS3Client(): S3Client = S3Client.builder().region(region).build()
}
