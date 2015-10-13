var ioc = {
    dataSource: {
        type: "com.alibaba.druid.pool.DruidDataSource",
        events: {
            depose: "close"
        },
        fields: {
            url: 'jdbc:hsqldb:mem:db',
            username: 'sa',
            password: 'sa',
            initialSize: 0,
            maxActive: 1,
            minIdle: 0,
            defaultAutoCommit: false,
            testWhileIdle: false
        }
    },
    dao: {
        type: "org.nutz.dao.impl.NutDao",
        fields: {
            dataSource: {
                refer: 'dataSource'
            }
        }
    }
};
