package net.maku.iot.communication;

import net.maku.iot.entity.IotDeviceEntity;
import net.maku.iot.enums.DeviceCommandEnum;
import net.maku.iot.communication.mqtt.dto.DeviceCommandResponseDTO;

/**
 * 基础通信协议具备功能
 */
public interface BaseCommunication {

    // 异步发送指令,不等待设备响应
    String asyncSendCommand(IotDeviceEntity device, DeviceCommandEnum command, String payload);

    //同步发送指定，等待设备响应
    DeviceCommandResponseDTO syncSendCommand(IotDeviceEntity device, DeviceCommandEnum command, String payload);

    //同步发送指定，等待设备响应，调试实现
    DeviceCommandResponseDTO syncSendCommandDebug(IotDeviceEntity device, DeviceCommandEnum command, String payload);

    //模拟设备属性上报
    void simulateDeviceReportAttributeData(IotDeviceEntity device, String payload);

    //模拟设备服务指令响应数据
    void simulateDeviceCommandResponseAttributeData(IotDeviceEntity device, String payload);


}
