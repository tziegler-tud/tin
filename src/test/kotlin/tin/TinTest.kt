package tin

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import tin.services.internal.DijkstraQueryAnsweringServiceTest
import tin.services.internal.ProductAutomatonServiceTest
import tin.services.internal.fileReaders.DatabaseReaderServiceTest
import tin.services.internal.fileReaders.QueryReaderServiceTest
import tin.services.internal.fileReaders.TransducerReaderServiceTest

@Service
class TinTest {

    @Autowired
    lateinit var productAutomatonServiceTest: ProductAutomatonServiceTest

    @Autowired
    lateinit var dijkstraQueryAnsweringServiceTest: DijkstraQueryAnsweringServiceTest

    @Autowired
    lateinit var databaseReaderServiceTest: DatabaseReaderServiceTest

    @Autowired
    lateinit var queryReaderServiceTest: QueryReaderServiceTest

    @Autowired
    lateinit var transducerReaderServiceTest: TransducerReaderServiceTest


    @Test
    fun testFileReaders() {
        // test 3 file readers

    }

    @Test
    fun testProductAutomatonBuilder() {
        // test 9 edge types

    }

    @Test
    fun testDijkstra() {
        // test 3 computation modes
    }

}