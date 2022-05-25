package nextstep.subway.line;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.Station;
import nextstep.subway.station.StationAcceptanceTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철노선 관련 기능")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LineAcceptanceTest {
    private final StationAcceptanceTest stationAcceptanceTest = new StationAcceptanceTest();

    @LocalServerPort
    int port;

    @BeforeEach
    public void setUp() {
        if (RestAssured.port == RestAssured.UNDEFINED_PORT) {
            RestAssured.port = port;
        }
    }

    /*
    * When 지하철 노선을 생성하면
    * Then 지하철 노선이 생성된다
    * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다
    */
    @Test
    void 지하철노선_생성() {
        // when
        ExtractableResponse<Response> upStation = stationAcceptanceTest.createStation("강남역");
        ExtractableResponse<Response> downStation = stationAcceptanceTest.createStation("역삼역");

        Map<String, Object> param = new HashMap<>();
        param.put("name", "신분당선");
        param.put("color", "bg-red-600");
        param.put("upStationId", upStation.jsonPath().get("id"));
        param.put("downStationId", downStation.jsonPath().get("id"));
        param.put("distance", 10);

        ExtractableResponse<Response> response =
                RestAssured.given().log().all()
                .body(param)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    /*
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @Test
    void 지하철노선_목록_조회() {
        // given
        stationAcceptanceTest.createStation("지하철역");
        stationAcceptanceTest.createStation("새로운지하철역");
        stationAcceptanceTest.createStation("또다른지하철역");

        Map<String, Object> line1 = new HashMap<>();
        line1.put("name", "신분당선");
        line1.put("color", "bg-red-600");
        line1.put("upStationId", 1);
        line1.put("downStationId", 2);
        line1.put("distance", 10);

        Map<String, Object> line2 = new HashMap<>();
        line2.put("name", "분당선");
        line2.put("color", "bg-green-600");
        line2.put("upStationId", 1);
        line2.put("downStationId", 3);
        line2.put("distance", 10);

        RestAssured.given().log().all()
                .body(line1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        RestAssured.given().log().all()
                .body(line2)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/lines")
                .then().log().all()
                .extract();
        List<String> lineNames = response.jsonPath().getList("name");

        // then
        assertThat(lineNames).contains("신분당선", "분당선");
    }

    /*
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @Test
    void 지하철노선_조회() {
        // given
        stationAcceptanceTest.createStation("지하철역");
        stationAcceptanceTest.createStation("새로운지하철역");

        Map<String, Object> line1 = new HashMap<>();
        line1.put("name", "신분당선");
        line1.put("color", "bg-red-600");
        line1.put("upStationId", 1);
        line1.put("downStationId", 2);
        line1.put("distance", 10);

        RestAssured.given().log().all()
                .body(line1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().get("/lines/{id}", 1L)
                .then().log().all()
                .extract();
        Line line = response.jsonPath().get();

        // then
        assertThat(line.getId()).isEqualTo(1L);
    }

    /*
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다
     */
    @Test
    void 지하철노선_수정() {
        // given
        stationAcceptanceTest.createStation("지하철역");
        stationAcceptanceTest.createStation("새로운지하철역");

        Map<String, Object> line1 = new HashMap<>();
        line1.put("name", "신분당선");
        line1.put("color", "bg-red-600");
        line1.put("upStationId", 1);
        line1.put("downStationId", 2);
        line1.put("distance", 10);

        RestAssured.given().log().all()
                .body(line1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // when
        Map<String, Object> putLine1 = new HashMap<>();
        line1.put("name", "다른분당선");
        line1.put("color", "bg-red-600");

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(putLine1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().put("/lines/{id}", 1L)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /*
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다
     */
    @Test
    void 지하철노선_삭제() {
        // given
        stationAcceptanceTest.createStation("지하철역");
        stationAcceptanceTest.createStation("새로운지하철역");

        Map<String, Object> line1 = new HashMap<>();
        line1.put("name", "신분당선");
        line1.put("color", "bg-red-600");
        line1.put("upStationId", 1);
        line1.put("downStationId", 2);
        line1.put("distance", 10);

        RestAssured.given().log().all()
                .body(line1)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines")
                .then().log().all()
                .extract();

        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .when().delete("/lines/{id}", 1L)
                .then().log().all()
                .extract();

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}
