package pub.timelyrain.wol.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

@RestController
public class WolController {
    private static final String MAGIC = "0xFFFFFFFFFFFF";
    @Value("${mac.default}")
    private String mac;

    private static byte[] hexToBinary(String hexString) {
        //1.定义变量：用于存储转换结果的数组
        byte[] result = new byte[hexString.length()];

        //2.去除字符串中的16进制标识"0X"并将所有字母转换为大写
        hexString = hexString.toUpperCase().replace("0X", "");

        //3.开始转换
        //3.1.定义两个临时存储数据的变量
        char tmp1 = '0';
        char tmp2 = '0';
        //3.2.开始转换，将每两个十六进制数放进一个byte变量中
        for (int i = 0; i < hexString.length(); i += 2) {
            result[i / 2] = (byte) ((hexToDec(tmp1) << 4) | (hexToDec(tmp2)));
        }
        return result;
    }

    private static byte hexToDec(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    @GetMapping("start")
    public String start() {
        return wol(mac.replace("-", ""));
    }

    @GetMapping("/start/{mac}")
    public String wol(@PathVariable String mac) {
        StringBuffer pocket = new StringBuffer();
        pocket.append(MAGIC);

        for (int i = 0; i < 16; i++)
            pocket.append(mac);

        try {
            byte[] command = hexToBinary(pocket.toString());
            InetAddress address = InetAddress.getByName("255.255.255.255");
            //2.获取广播socket
            MulticastSocket socket = new MulticastSocket(9);
            DatagramPacket packet = new DatagramPacket(command, command.length, address, 9);
            //4.发送数据
            socket.send(packet);
            //5.关闭socket
            socket.close();

            return "started";
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
