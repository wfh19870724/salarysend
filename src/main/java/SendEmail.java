
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;


import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.log4j.Logger;


public class SendEmail {
    private boolean flag = false;
    private JTextField name;
    private JTextField pwd;
    private JComboBox smtp;
    private JTextField title;
    private String excelName;
    private JCheckBox sendSelf;
    private Logger logger = Logger.getLogger(SendEmail.class);
    public void init() {
        logger.info("窗口初始化开始");
        JFrame frame = new JFrame("工资条发放工具"); // 创建Frame窗口
        frame.setSize(600, 400);
        JPanel jp = new JPanel(); // 创建JPanel对象

        JLabel lname = new JLabel("邮箱:");
        lname.setHorizontalAlignment(SwingConstants.RIGHT);
        name = new JTextField(30);
        jp.add(lname, BorderLayout.WEST);
        jp.add(name, BorderLayout.EAST);

        JLabel lpwd = new JLabel("授权码:");
        lpwd.setHorizontalAlignment(SwingConstants.RIGHT);
        pwd = new JPasswordField(20);
        jp.add(lpwd, BorderLayout.WEST);
        jp.add(pwd, BorderLayout.EAST);

        JLabel lsmtp = new JLabel("邮箱服务:");
        lsmtp.setHorizontalAlignment(SwingConstants.RIGHT);
        String[] smtpList = {"smtp.163.com","smtp.qq.com"};
        smtp = new JComboBox(smtpList);
        jp.add(lsmtp, BorderLayout.WEST);
        jp.add(smtp, BorderLayout.EAST);

        JLabel ltitle = new JLabel("邮件标题:");
        ltitle.setHorizontalAlignment(SwingConstants.RIGHT);
        title= new JTextField(20);
        jp.add(ltitle, BorderLayout.WEST);
        jp.add(title, BorderLayout.EAST);

        JLabel lc = new JLabel("抄送自己:");
        sendSelf = new JCheckBox("") ;
        lc.setHorizontalAlignment(SwingConstants.RIGHT);
        jp.add(lc, BorderLayout.WEST);
        jp.add(sendSelf, BorderLayout.EAST);


        JButton developer = new JButton("导入工资");
        developer.setHorizontalAlignment(SwingConstants.CENTER);
        jp.add(developer, BorderLayout.SOUTH);
        developer.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                flag = eventOnImport(new JButton());
            }
        });
        JButton send = new JButton("发送邮件");
        send.setHorizontalAlignment(SwingConstants.CENTER);
        jp.add(send, BorderLayout.SOUTH);
        send.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                send(name.getText(), pwd.getText(),  (String) smtp.getSelectedItem());
            }
        });

        jp.setLayout(new GridLayout(6, 2, 0, 0));
        frame.add(jp);
        frame.setBounds(300, 200, 600, 300);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        logger.info("窗口初始化结束");
    }

    public static void main(String[] args) {
        new SendEmail().init();

    }

    /**
     *   * 文件上传功能   *   * @param developer   *            按钮控件名称  
     */
    public boolean eventOnImport(JButton developer) {
        logger.info("文件上传开始");
        JFileChooser chooser = new JFileChooser();
        chooser.setMultiSelectionEnabled(true);
        /** 过滤文件类型 * */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("xls",
                "xls","xlsx");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(developer);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            /** 得到选择的文件* */
            File arrfiles = chooser.getSelectedFile();
            if (arrfiles == null || arrfiles.length() == 0) {
                return false;
            }
            checkBookType(arrfiles.getName());
            FileInputStream input = null;
            FileOutputStream out = null;
            String path = "./";
            try {
                input = new FileInputStream(arrfiles);
                byte[] buffer = new byte[1024];
                File des = new File(path, arrfiles.getName());
                out = new FileOutputStream(des);
                int len;
                while (-1 != (len = input.read(buffer))) {
                    out.write(buffer, 0, len);
                }
                out.close();
                input.close();
                logger.info("文件上传成功，文件名："+arrfiles.getName());
                JOptionPane.showMessageDialog(null, "上传成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
                excelName=arrfiles.getName();
                return true;
            } catch (FileNotFoundException e1) {
                logger.error("文件上传失败"+ e1.getMessage());
                JOptionPane.showMessageDialog(null, "上传失败！", "提示",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            } catch (IOException e1) {
                logger.error("文件上传失败"+ e1.getMessage());
                JOptionPane.showMessageDialog(null, "上传失败！", "提示",
                        JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }finally {
                try {
                    if (input != null){
                        input.close();
                    }
                    if (out != null) {
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return false;
    }

    private void checkBookType(String name) {
        if (name.toLowerCase().endsWith("xlsx")) {
            logger.info("文件类型正确");
        } else if (name.toLowerCase().endsWith("xls")) {
            logger.info("文件类型正确");
        } else {
            //  抛出自定义的业务异常
            throw new RuntimeException("excel格式文件错误");
        }
    }


    private void send(final String emailMame, final String emailPwd, String emailSmtp) {
        if (flag) {
            try {
                logger.info("邮件发送开始，发送账号："+emailMame+",授权码："+emailPwd+",邮件服务："+emailSmtp);
                //跟smtp服务器建立一个连接
                Properties p = new Properties();
                //设置邮件服务器主机名
                p.setProperty("mail.host",emailSmtp);
                //发送服务器需要身份验证,要采用指定用户名密码的方式去认证
                p.setProperty("mail.smtp.auth", "true");
                //发送邮件协议名称
                p.setProperty("mail.transport.protocol", "smtp");

                //开启SSL加密，否则会失败
                MailSSLSocketFactory sf = new MailSSLSocketFactory();
                sf.setTrustAllHosts(true);
                p.put("mail.smtp.ssl.enable", "true");
                p.put("mail.smtp.ssl.socketFactory", sf);
                p.put("userName",emailMame); //这里填上你的邮箱（发送方）为了解决554错误
                // 创建session
                Session session = Session.getInstance(p, new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        //用户名可以用QQ账号也可以用邮箱的别名:第一个参数为邮箱账号,第二个为授权码
                        return new PasswordAuthentication(emailMame,emailPwd);
                    }
                });
                //设置打开调试状态
                session.setDebug(true);
                //声明一个Message对象(代表一封邮件),从session中创建
                MimeMessage msg = new MimeMessage(session);
                //邮件信息封装
                //1发件人
                msg.setFrom(new InternetAddress(emailMame));
                if(sendSelf.isSelected()){
                    logger.info("抄送自己");
                    msg.addRecipients(MimeMessage.RecipientType.CC, InternetAddress.parse(p.getProperty("userName")));
                }
                //3邮件内容:主题、内容
                msg.setSubject(title.getText());
                //2收件人:可以多个
                //一个的收件人
                List<String > list = new ReadFile().readExcel("./"+excelName);
                String title = list.get(0);
                String[] temp = title.split(",");
                StringBuilder sb = new StringBuilder();
                sb.append("<table border=\"1\" cellspacing=\"0\"><tr>");
                for(int i = 0;i<temp.length-1;i++){
                    sb.append("<td>").append(temp[i]).append("</td>");
                }
                sb.append("</tr>");

                for (int i =1 ;i<list.size();i++){
                    String[] info = list.get(i).split(",");
                    if(null==info||info.length==0){
                        continue;
                    }
                    StringBuilder text = new StringBuilder();
                    text.append(sb.toString()).append("<tr>");
                    for(int j = 0;j<info.length-1;j++){
                        text.append("<td>").append(info[j]).append("</td>");
                    }
                    text.append("</tr></table>");
                    String mailInfo = text.toString();
                    String revMail = info[info.length-1];

                   msg.setRecipient(Message.RecipientType.TO, new InternetAddress(revMail));
                    msg.setContent(mailInfo,"text/html;charset=utf-8");//发html格式的文本
                    //发送动作
                    Transport.send(msg);
                    logger.info(revMail+"发送成功");
                }
                logger.info("全部发送成功");
                JOptionPane.showMessageDialog(null, "邮件发送成功！", "提示",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                logger.error("邮件发送失败:"+e);
                JOptionPane.showMessageDialog(null, "邮件发送失败！", "提示",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }else{
            JOptionPane.showMessageDialog(null, "先上传工资表！", "提示",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
