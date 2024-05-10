package com.zsh.spider;

import com.zsh.spider.dao.SpiderResultDao;
import com.zsh.spider.enums.LimitTimeEnum;
import com.zsh.spider.pojo.dto.SearchDTO;
import com.zsh.spider.pojo.dto.SpiderDTO;
import com.zsh.spider.pojo.po.SpiderTaskPO;
import com.zsh.spider.pojo.po.TaskPO;
import com.zsh.spider.service.SpiderDispatcherService;
import com.zsh.spider.service.TaskDispatcherService;
import com.zsh.spider.service.TaskStateService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * 搜索测试
 *
 * @author zsh
 * @version 1.0.0
 * @date 2022/09/27 18:26
 */
@SpringBootTest
@MockBean({SpiderDispatcherService.class})
public class SearchTest {

    @Autowired
    private TaskDispatcherService taskDispatcherService;
    @Autowired
    private SpiderDispatcherService daemonSpiderService;
    @Autowired
    private SpiderResultDao spiderResultDao;
    @MockBean
    private TaskStateService taskStateService;

    @BeforeEach
    void before() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("深圳10年房价走势").setLimitTime(LimitTimeEnum.YEAR);
        TaskPO taskPO = new TaskPO();
        taskPO.setId(-1L);
        taskPO.setSearchParam(searchDTO);
        Mockito.doReturn(taskPO).when(taskStateService).getTask(-1L);
    }

    /**
     * 搜索
     */
    @SneakyThrows
    @Test
    public void testSearch() {
        SearchDTO searchDTO = new SearchDTO().setKeyWord("深圳10年房价走势").setLimitTime(LimitTimeEnum.YEAR);
        taskDispatcherService.startSearch(searchDTO);
        Thread.sleep(1000 * 3600);
    }

    /**
     * 搜索重定向测试
     */
    @SneakyThrows
    @Test
    public void testSpiderSearchRedirect() {
        spiderResultDao.deleteByTaskId(-1L);
        SpiderDTO spiderDTO = new SpiderDTO("https://www.sogou.com/link?url=hedJjaC291P1T5YDoUsG2yEbrCWjzgVKCm-WZSEv6o9aLGXOw6Z_-idBg3chOzStkYEI3buCqkaARVTwvv8Uzg..", 0, "testname");
        daemonSpiderService.addSpiderTask(new SpiderTaskPO(-1L, spiderDTO));
        Thread.sleep(1000 * 3600);
    }

    /**
     * 搜索重定向测试
     */
    @SneakyThrows
    @Test
    public void testSpiderSearchRedirect2() {
        spiderResultDao.deleteByTaskId(-1L);
        SpiderDTO spiderDTO = new SpiderDTO("https://www.chinaso.com/link?url=qxXrebxECaVrLSafO5%2F1wHhFFc4C5WxjKSyzopsxCAIOiNFEQwLgujJ9%2FV4ZS4S%2F&pos=49&wd=%E6%B7%B1%E5%9C%B310%E5%B9%B4%E6%88%BF%E4%BB%B7%E8%B5%B0%E5%8A%BF", 0, "testname");
        daemonSpiderService.addSpiderTask(new SpiderTaskPO(-1L, spiderDTO));
        Thread.sleep(1000 * 3600);
    }

    /**
     * 内容乱码
     */
    @SneakyThrows
    @Test
    public void testSpiderSearchContentCharset() {
        spiderResultDao.deleteByTaskId(-1L);
        SpiderDTO spiderDTO = new SpiderDTO("https://fangjia.fang.com/sz/a085/", 0, "testname");
        daemonSpiderService.addSpiderTask(new SpiderTaskPO(-1L, spiderDTO));
        Thread.sleep(1000 * 3600);
    }


    /**
     * 内容乱码
     */
    @SneakyThrows
    @Test
    public void testSpiderSearchContentCharset2() {
        spiderResultDao.deleteByTaskId(-1L);
        SpiderDTO spiderDTO = new SpiderDTO("http://house.people.com.cn/n/2015/0906/c164220-27546212.html", 0, "testname");
        daemonSpiderService.addSpiderTask(new SpiderTaskPO(-1L, spiderDTO));
        Thread.sleep(1000 * 3600);
    }

}
