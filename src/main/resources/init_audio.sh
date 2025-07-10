#!/bin/bash

# 设置默认声卡为 card 1（nau8822）
echo "配置默认声卡..."
cat <<EOF | sudo tee /etc/asound.conf
pcm.!default {
    type hw
    card 1
    device 0
}
ctl.!default {
    type hw
    card 1
}
EOF

# 设置播放音量：耳机与扬声器全开，音量调至中高（可调）
echo "设置播放音量..."
amixer -c 1 sset 'Headphone' 63 unmute
amixer -c 1 sset 'Speaker' 63 unmute
amixer -c 1 sset 'PCM' 255

# 设置录音音量（MIC）相关通路
echo "设置录音路径和音量..."
amixer -c 1 sset 'ADC' 255
amixer -c 1 sset 'PGA' 30
amixer -c 1 sset 'PGA Boost' 1
amixer -c 1 sset 'Main Mic' on
amixer -c 1 sset 'Left Input Mixer MicN' on
amixer -c 1 sset 'Left Input Mixer MicP' on
amixer -c 1 sset 'Right Input Mixer MicN' on
amixer -c 1 sset 'Right Input Mixer MicP' on

# 设置输出通路（从 DAC 到耳机/扬声器/AUX）
echo "设置播放路径..."
amixer -c 1 sset 'Left Output Mixer LDAC' on
amixer -c 1 sset 'Right Output Mixer RDAC' on
amixer -c 1 sset 'AUX1 Output Mixer LDAC' on
amixer -c 1 sset 'AUX2 Output Mixer LDAC' on
amixer -c 1 sset 'AUXOUT' on

# 关闭不必要的混音/反向输入，避免干扰
echo "关闭无用项..."
amixer -c 1 sset 'Digital Loopback' off
amixer -c 1 sset 'Headphone ZC' off
amixer -c 1 sset 'Speaker ZC' off

echo "初始化完成。你现在可以播放和录音了。"
