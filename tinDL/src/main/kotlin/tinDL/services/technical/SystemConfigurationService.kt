package tinDL.services.technical

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import java.nio.file.Path

@Service
@ConfigurationProperties(prefix = "tin")
@Configuration("tin")
class SystemConfigurationService {

    private var projectRoot: String?= null;
    private var fileDir: String?= null;
    private var uploadDir: String?= null;
    private var databaseFilesDirName: String = "/databases";
    private var queryFilesDirName: String = "/queries";
    private var conjunctiveQueryFilesDirName: String = "/conjunctiveQueries";
    private var transducerFilesDirName: String = "/transducers";
    private var ontologyFilesDirName: String = "/ontology";

    private var databaseSizeLimit: Int = 1024000
    private var querySizeLimit: Int = 12800024
    private var transducerSizeLimit: Int = 128000
    private var ontologySizeLimit: Int = 512000


//
//    private var uploadPathForQueries = "$projectRoot/src/main/resources/input/queries/"
//    private var uploadPathForDatabases = "$projectRoot/src/main/resources/input/databases/"
//    private var uploadPathForTransducers = "$projectRoot/src/main/resources/input/transducers/"

    public fun getProjectRoot():String? {
        return this.projectRoot;
    }

    /**
     * returns the current project root as defined in application.properties
     * returns empty string if project root is not set.
     */
    public fun getProjectRootSave():String {
        return this.projectRoot ?: ""
    }

    fun setProjectRoot(string: String) {
        this.projectRoot = string;
    }

    fun getFileDir():String? {
        return this.fileDir
    }

    fun setFileDir(string: String) {
        this.fileDir = string;
    }

    fun getUploadDir():String? {
        return this.uploadDir
    }

    fun setUploadDir(string: String) {
        this.uploadDir = string;
    }

    fun getDatabasePath():String {
        return Path.of(this.projectRoot + this.fileDir + this.databaseFilesDirName).toString();
    }

    fun getQueryPath():String{
        return Path.of(this.projectRoot + this.fileDir + this.queryFilesDirName).toString();
    }

    fun getConjunctiveQueryPath():String{
        return Path.of(this.projectRoot + this.fileDir + this.conjunctiveQueryFilesDirName).toString();
    }
    fun getTransducerPath():String {
        return Path.of(this.projectRoot + this.fileDir + this.transducerFilesDirName).toString();
    }

    fun getOntologyPath():String{
        return Path.of(this.projectRoot + this.fileDir + this.ontologyFilesDirName).toString();
    }

    fun getUploadParentPath():String{
        return Path.of(this.projectRoot + this.uploadDir).toString();
    }

    fun getUploadQueryPath():String{
        return Path.of(this.projectRoot + this.uploadDir + this.queryFilesDirName).toString();
    }

    fun getUploadTransducerPath():String{
        return Path.of(this.projectRoot + this.uploadDir + this.transducerFilesDirName).toString();
    }

    fun getUploadOntologyPath():String{
        return Path.of(this.projectRoot + this.uploadDir + this.ontologyFilesDirName).toString();
    }

    fun getDatabaseSizeLimit():Int {
        return this.databaseSizeLimit;
    }

    fun setDatabaseSizeLimit(limit: Int) {
        this.databaseSizeLimit = limit;
    }

    fun getQuerySizeLimit():Int {
        return this.querySizeLimit;
    }

    fun setQuerySizeLimit(limit: Int) {
        this.querySizeLimit = limit;
    }

    fun getTransducerSizeLimit():Int {
        return this.transducerSizeLimit;
    }

    fun setTransducerSizeLimit(limit: Int) {
        this.transducerSizeLimit = limit;
    }

    fun getOntologySizeLimit():Int {
        return this.ontologySizeLimit;
    }

    fun setOntologySizeLimit(limit: Int) {
        this.ontologySizeLimit = limit;
    }



}