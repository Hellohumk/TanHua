package com.tanhua;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class TanHuaApplicationTests {

//    @Test
//    public void testMongoDBData() {
//        for (int i = 2; i < 100; i++) {
//            int score = (int) (Math.random()*100);
//            System.out.println("db.recommend_user.insert({\"userId\":" + i +
//                    ",\"toUserId\":1,\"score\":"+score+",\"date\":\"2019/1/1\"})");
//        }
//    }

    @Test
    public void testMySQLData(){
        System.out.println("INSERT INTO `tb_user` (`id`, `mobile`," +
                "`password`, `created`, `updated`) VALUES ('1', '17602026868'," +
                        "'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:43:46', '2019-08-02 " +
                "16:43:46');");
        System.out.println("INSERT INTO `tb_user` (`id`, `mobile`," +
                "`password`, `created`, `updated`) VALUES ('2', '15800807988'," +
                        "'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:50:32', '2019-08-02 " +
                "16:50:32');");
        for (int i = 3; i < 100; i++) {
            String mobile = "13"+RandomStringUtils.randomNumeric(9);
            System.out.println("INSERT INTO `tb_user` (`id`, `mobile`,
                    `password`, `created`, `updated`) VALUES ('"+i+"', '"+mobile+"',
                    'e10adc3949ba59abbe56e057f20f883e', '2019-08-02 16:43:46', '2019-08-02
                    16:43:46');");
        }
    }

}
