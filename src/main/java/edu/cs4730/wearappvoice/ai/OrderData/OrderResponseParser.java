package edu.cs4730.wearappvoice.ai.OrderData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class OrderResponseParser {

    private FullResponse responseData;      // 完整响应对象
    private List<Order> orderList = new ArrayList<>();  // 所有订单

    public boolean parseAndStore(String json) {
        responseData = parseJson(json);
        orderList.clear();  // 每次重新解析前清空旧订单

        if (responseData == null) {
            System.err.println("解析失败：返回数据为空或格式错误。");
            return false;
        }

        System.out.println("输入: " + safeStr(responseData.input));
        System.out.println("结果: " + safeStr(responseData.text));

        if (responseData.cmd != null) {
            System.out.println("命令: " + safeStr(responseData.cmd.cmd));
            if (responseData.cmd.args != null) {
                System.out.println("时间范围: " + safeStr(responseData.cmd.args.from) + " 到 " + safeStr(responseData.cmd.args.to));
            }

            if (responseData.cmd.result != null && responseData.cmd.result.data != null) {
                System.out.println("订单数: " + responseData.cmd.result.data.size());
                for (Order order : responseData.cmd.result.data) {
                    System.out.println("运单ID: " + order.waybillId + ", 状态: " + safeStr(order.waybillStatus) +
                            ", 日期: " + safeStr(order.waybillDate));

                    // 添加到列表中供外部使用
                    orderList.add(order);
                }
            } else {
                System.out.println("没有找到订单数据。");
            }
        } else {
            System.out.println("无 cmd 命令信息");
        }

        return true;
    }

    private static FullResponse parseJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, FullResponse.class);
        } catch (JsonMappingException e) {
            System.err.println("映射失败（字段类型不匹配）: " + e.getMessage());
        } catch (JsonProcessingException e) {
            System.err.println("JSON处理异常: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("未知解析异常: " + e.getMessage());
        }
        return null;
    }

    private static String safeStr(Object s) {
        return s == null ? "(空)" : s.toString();
    }

    // 对外提供访问解析结果的方法
    public FullResponse getResponseData() {
        return responseData;
    }

    public Cmd getCmd() {
        return responseData != null ? responseData.cmd : null;
    }

    public Result getResult() {
        return (responseData != null && responseData.cmd != null) ? responseData.cmd.result : null;
    }

    public java.util.List<Order> getOrders() {
        Result result = getResult();
        return result != null ? result.data : null;
    }
}
