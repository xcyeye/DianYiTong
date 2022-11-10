package xyz.xcye.zigongai;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

@Slf4j
public class Demo1 {
    public static void main(String[] args) throws Exception {
        String userId = "4406772";
        // 查看宫颈癌列表
        String gjaList = "https://newdytapi.ynhdkc.com/index/Vaccine/hpvhoslist";
        RestTemplate template = new RestTemplate();
        Map<String, Object> gjaListMap = template.getForObject(new URI(gjaList), Map.class);
        ArrayList<Object> gjaListMapData = (ArrayList) gjaListMap.get("data");
        log.info("查询到{}个医院有宫颈癌疫苗\n", gjaListMapData.size());

        // 查询所有有宫颈癌疫苗的医院
        for (Object data : gjaListMapData) {
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) data;
            log.info("查询到: {}医院有疫苗，地址为: {}, 正在尝试预约该医院的疫苗", map.get("hos_name"), map.get("hos_address"));

            String hosId = (String) map.get("hos_id");
            Integer hosCode = (Integer) map.get("hos_code");
            ArrayList<Object> doctorList = (ArrayList<Object>) map.get("doctor");

            // 查询每一个医院的信息
            for (Object doctorInfo : doctorList) {
                LinkedHashMap<String, Integer> info = (LinkedHashMap<String, Integer>) doctorInfo;
                Integer depId = info.get("dep_id");
                Integer docId = info.get("doc_id");

                // 查询某个医院的某个疫苗
                String hosYmInfoUrl = "https://newdytapi.ynhdkc.com/index/doctor/" + docId + "?hos_code=" + hosCode + "&dep_id=" + depId + "&vip=0";

                // 查询每个医院的宫颈癌疫苗的信息 是几价的
                RestTemplate hosYmInfoTemp = new RestTemplate();
                Map hosYmInfoTempData = hosYmInfoTemp.getForObject(new URI(hosYmInfoUrl), Map.class);
                LinkedHashMap<String, Object> hosYmInfoTempDataMap = (LinkedHashMap<String, Object>) hosYmInfoTempData.get("data");
                String  docName = (String) hosYmInfoTempDataMap.get("doc_name");
                // "九价宫颈癌疫苗"
                if ("四价宫颈癌疫苗".equals(docName)) {
                    log.info("{} 存在九价疫苗，正在获取该医院所有预约时间点", map.get("hos_name"));
                    // 查询此疫苗的时间段
                    String ymScheduleUrl = "https://newdytapi.ynhdkc.com/index/schedule?hos_code="+ hosCode +"&dep_id="+ depId +"&doc_id="+ docId +"&from_date=2022-11-10&end_date=2022-11-18&reg_date=2017-2-20&hyid=&vip=0";
                    RestTemplate ymScheduleTemplate = new RestTemplate();
                    Map ymScheduleMapData = ymScheduleTemplate.getForObject(new URI(ymScheduleUrl), Map.class);
                    ArrayList<Object> ymScheduleList = (ArrayList<Object>) ymScheduleMapData.get("data");

                    // 获取预约人信息
                    String personInfoUrl = "https://newdytapi.ynhdkc.com/index/patient/"+ userId +"?hos_id="+ hosId;
                    RestTemplate personInfoRestTemplate = new RestTemplate();

                    HttpHeaders headers = new HttpHeaders();
                    headers.add("x-uuid", "5ABFBAF6DDF0A10162FDBC8FD425EA4F");
                    headers.add("Authorization", "DYT eyJhbGciOiJIUzI1NiJ9.eyJ3ZWNoYXRfaWQiOjU5ODcyOTUsInN1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZSI6MCwib3BlbmlkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjoxLCJuZXdfb3BlbmlkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInVzZXJfaWQiOjQ0MDY3NzIsIndlY2hhdF9vcGVuX2lkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInVuaW9uX2lkIjoib05RejQwYWYzTDM1MXcwdG5kRWpzMlNPT0hfNCIsIm1vY2tfb3BlbmlkIjpmYWxzZSwibWluaV9vcGVuaWQiOiIiLCJleHAiOjE2NjgwNjU2MTUsImlhdCI6MTY2ODA2MDAxNX0.jaHbpeSBIQhGeSFQlBz2Wdcb3gNk4zgPDUV97NNgdTg");
                    headers.add("Host", "newdytapi.ynhdkc.com");
                    HttpEntity<Object> objectHttpEntity = new HttpEntity<>(headers);

                    ResponseEntity<Map> entity = personInfoRestTemplate.exchange(personInfoUrl, HttpMethod.GET, objectHttpEntity, Map.class, new HashMap<>());
                    Map personInfoBody = entity.getBody();
                    Integer code = (Integer) personInfoBody.get("code");
                    if (code == 0) {
                        throw new Exception(personInfoBody.get("msg").toString());
                    }
                    ArrayList personInfoDataList = (ArrayList) personInfoBody.get("data");
                    LinkedHashMap personInfo = (LinkedHashMap) personInfoDataList.get(0);
                    Integer patId = (Integer) personInfo.get("pat_id");
                    Integer personUserId = (Integer) personInfo.get("user_id");
                    // 打印个人信息
                    log.info("你的个人信息为{}", personInfo);

                    for (Object ymSchedule : ymScheduleList) {
                        LinkedHashMap<String, Object> ymScheduleMap = (LinkedHashMap<String, Object>) ymSchedule;
                        Integer scheduleId = (Integer) ymScheduleMap.get("schedule_id");
                        Object cateName = ymScheduleMap.get("cate_name");
                        Object schDate = ymScheduleMap.get("sch_date");
                        String timeType = (String) ymScheduleMap.get("time_type");
                        Integer srcNum = (Integer) ymScheduleMap.get("src_num");
                        if (srcNum == 0) {
                            log.warn("医院 {} 在{} - {} 时间段没有{} 疫苗，正在查询下一个时间段", map.get("hos_name"), schDate, cateName,docName);
                            continue;
                        }

                        log.info("正在尝试预约{} 医院，打疫苗时间为{} {} 的疫苗", map.get("hos_name"), schDate, cateName);

                        String yuYueUrl = "https://dytapi.ynhdkc.com/v1/appoint?hos_code="+ hosCode +"&dep_id="+ depId +"&doc_id="+ docId +"&pat_id="+ patId +"&user_id="+ userId +"&schedule_id="+ scheduleId +"&cate_name=&sch_date="+ schDate +"&time_type="+ timeType;


                        RestTemplate yuYueRestTemplate = new RestTemplate();

                        HttpHeaders yuYueHeaders = new HttpHeaders();
                        yuYueHeaders.add("x-uuid", "2CFF25CC2A3BF206BE0D0CA343F5113D");
                        yuYueHeaders.add("Authorization", "DYT eyJhbGciOiJIUzI1NiJ9.eyJ3ZWNoYXRfaWQiOjU5ODcyOTUsInN1YnNjcmliZSI6MCwiZHpqX3N1YnNjcmliZSI6MCwib3BlbmlkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInRoaXJkX3VzZXJfaWQiOiIiLCJpc3MiOiJkeXQiLCJuZXdfc3Vic2NyaWJlIjoxLCJuZXdfb3BlbmlkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInVzZXJfaWQiOjQ0MDY3NzIsIndlY2hhdF9vcGVuX2lkIjoibzdMQ1g2T2daM1BOVGlzRXpSV2VnTmdNQmY5ZyIsInVuaW9uX2lkIjoib05RejQwYWYzTDM1MXcwdG5kRWpzMlNPT0hfNCIsIm1vY2tfb3BlbmlkIjpmYWxzZSwibWluaV9vcGVuaWQiOiIiLCJleHAiOjE2NjgwNjU2MTUsImlhdCI6MTY2ODA2MDAxNX0.jaHbpeSBIQhGeSFQlBz2Wdcb3gNk4zgPDUV97NNgdTg");
                        yuYueHeaders.add("Host", "dytapi.ynhdkc.com");
                        HttpEntity<Object> yuYueHttpEntity = new HttpEntity<>(headers);

                        ResponseEntity<Map> yuYueEntity = yuYueRestTemplate.exchange(yuYueUrl, HttpMethod.POST, yuYueHttpEntity, Map.class, new HashMap<>());
                        Map yuYueMap = yuYueEntity.getBody();
                        Integer yuYueCode = (Integer) yuYueMap.get("code");
                        if (yuYueCode == 1) {
                            // 预约成功
                            return;
                        }else {
                            // 预约失败
                            log.warn("预约失败{}, 正在尝试预约下一个时间段\n\n\n", yuYueMap.get("msg"));
                        }
                    }
                }
            }
        }
    }
}
