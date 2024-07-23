package net.maku.iot.mqtt.handler;

import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.maku.framework.common.utils.JsonUtils;
import net.maku.iot.enums.DeviceTopicEnum;
import net.maku.iot.mqtt.dto.DeviceCommandResponseDTO;
import net.maku.iot.mqtt.factory.DeviceCommandResponseHandlerFactory;
import net.maku.iot.mqtt.service.DeviceMqttService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 设备命令响应处理器
 *
 * @author LSF maku_lsf@163.com
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceCommandResponseMqttMessageHandler implements MqttMessageHandler {
    private final DeviceCommandResponseHandlerFactory deviceCommandResponseHandlerFactory;


    private final DeviceMqttService deviceMqttService;

    @Override
    public boolean supports(String topic) {
        return DeviceTopicEnum.startsWith(topic, DeviceTopicEnum.COMMAND_RESPONSE.getTopic());
    }

    @Override
    public void handle(String topic, String message) {
        DeviceCommandResponseDTO commanddResponseDTO = parseCommandReplyMessage(topic, message);
        Optional.ofNullable(commanddResponseDTO.getCommand())
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("缺失指令类型! 主题:'{}',消息:{}", topic, message)));
        Optional.ofNullable(commanddResponseDTO.getCommandId())
                .orElseThrow(() -> new IllegalArgumentException(StrUtil.format("缺失指令ID! 主题:'{}',消息:{}", topic, message)));

        Optional.ofNullable(commanddResponseDTO)
                .ifPresent(responseDTO -> {
                    // 调用设备命令执行器的命令响应处理逻辑
                    try {
                        deviceMqttService.commandReplied(topic, responseDTO);
                    } catch (Exception e) {
                        log.error(StrUtil.format("调用设备命令执行器响应处理方法出错，topic:{}, message:{}", topic, message), e);
                    }
                    // 调用自定义命令响应处理器
                    try {
                        deviceCommandResponseHandlerFactory.getHandlers().forEach(h -> h.handle(topic, responseDTO));
                    } catch (Exception e) {
                        log.error(StrUtil.format("调用设备命令响应响应处理器出错，topic:{}, message:{}", topic, message), e);
                    }
                });
    }

    private DeviceCommandResponseDTO parseCommandReplyMessage(String topic, String message) {
        try {
            DeviceCommandResponseDTO commandResponse = JsonUtils.parseObject(message, DeviceCommandResponseDTO.class);
            if (StrUtil.isBlank(commandResponse.getCommandId())) {
                log.error(StrUtil.format("主题'{}'的消息,缺失指令ID", topic));
                return null;
            }
            return commandResponse;

        } catch (Exception e) {
            log.error(StrUtil.format("将主题'{}'的消息解析为设备命令响应对象失败", topic), e);
            return null;
        }
    }
}