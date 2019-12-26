import com.porsche.ecom.retoure.services.JsonParserService
import com.porsche.ecom.retoure.services.impl.JsonParserServiceImpl
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets


class JsonParserServiceImplTest {
    private lateinit var jsonParserService: JsonParserService

    @Before
    fun setUp() {
        jsonParserService = JsonParserServiceImpl()
    }

    @Test
    fun `Assert that messageId is successfully parsed from JSON`() {
        val json = "{\"Records\": [{\"ses\": {\"mail\": {\"messageId\": \"TEST\"}}}]}"

        val result =
            jsonParserService.parseMessageIdFrom(ByteArrayInputStream(json.toByteArray(StandardCharsets.UTF_8)))

        assertEquals("TEST", result)
    }

    @Test
    fun mockTest() {
        val mockedList: List<*> = mock(ArrayList::class.java)
        mockedList.size
        verify(mockedList).size
    }
}