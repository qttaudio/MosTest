# MosTest

本项目为对比测试QttAudio 与 Agora 客观音质MOS分值时使用demo程序。

# 测试步骤

### 准备阶段
1.编译 visqol 程序
2.拉取 QttAudioDemo，并编译
3.拉取 AgoraDemo,并编译

### 产生样本
1.将Demo分别安装到两台手机

2.两个客户端加入到一个房间，并在一端不停的发出人声，持续10~20s，关闭程序

3.使用adb 将应用程序目录下Crash目录的\*_receive.raw和\*_record.raw上传到Visqol所在目录

4.使用ffmpeg将raw文件转成wav文件
```
ffmpeg -f s16le -ar 48000 -ac 2 -i *_receive.raw receive.wav
ffmpeg -f s16le -ar 48000 -ac 2 -i *_record.raw record.wav
```
### 计算MOS_LQO分值
1.使用visqol计算MOS_LQO分值

```
 ./bazel-bin/visqol --reference_file record.wav --degraded_file receive.wav  --verbose
```
2.将QttAudioDemo产生的MOS_LQO分值与Agora产生的MOS_LQO分值对比


