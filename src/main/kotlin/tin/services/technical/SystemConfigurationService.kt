package tin.services.technical

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
    private var databaseFilesDirName: String = "/databases";
    private var queryFilesDirName: String = "/queries";
    private var transducerFilesDirName: String = "/transducers";

//
//    private var uploadPathForQueries = "$projectRoot/src/main/resources/input/queries/"
//    private var uploadPathForDatabases = "$projectRoot/src/main/resources/input/databases/"
//    private var uploadPathForTransducers = "$projectRoot/src/main/resources/input/transducers/"


    public fun getProjectRoot():String? {
        return this.projectRoot;
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

    fun getDatabasePath():String {
        return Path.of(this.projectRoot + this.fileDir + this.databaseFilesDirName).toString();
    }

    fun getQueryPath():String{
        return Path.of(this.projectRoot + this.fileDir + this.queryFilesDirName).toString();
    }
    fun getTransducerPath():String {
        return Path.of(this.projectRoot + this.fileDir + this.transducerFilesDirName).toString();
    }

}