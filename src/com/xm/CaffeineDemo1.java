package com.xm;

import com.github.benmanes.caffeine.cache.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class CaffeineDemo1 {
    public static String getCurrentTimeStr(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * 测试缓存数据小于maximumSize时, 是否会触发淘汰
     * 结果: 不会触发淘汰
     */
    public void test1(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(3).
                build(new CacheLoader<String, String>() {
                          @Override
                          public String load(String key) {
                              System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                              try {
                                  Thread.sleep(5000);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                              System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                              return String.valueOf(getCurrentTimeStr());
                          }
                      }
                );

        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("k1-->: "+cache.get("k1"));
        }
    }

    /**
     * 测试缓存数据大于maximumSize时, 是否会触发淘汰
     * 结果: 会触发
     */
    public void test2(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(3).
                build(new CacheLoader<String, String>() {
                          @Override
                          public String load(String key) {
                              System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                              try {
                                  Thread.sleep(5000);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                              System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                              return String.valueOf(getCurrentTimeStr());
                          }
                      }
                );

        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("k1-->: "+cache.get("k1"));
            System.out.println("k2-->: "+cache.get("k2"));
            System.out.println("k3-->: "+cache.get("k3"));
            System.out.println("k4-->: "+cache.get("k4"));
        }
    }

    /**
     * 不配置时间失效规则, 测试缓存数据小于maximumSize时, 多线程load数据是否会被阻塞?
     * 结果: 不会(Caffeine版本>=2.8.6)
     */
    public void test3(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(3).
                build(new CacheLoader<String, String>() {
                          @Override
                          public String load(String key) {
                              System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                              try {
                                  Thread.sleep(5000);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                              System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                              return String.valueOf(getCurrentTimeStr());
                          }
                      }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("k1-->: "+cache.get("k1"));
                }
            }
        });
        Thread threadT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("k2-->: "+cache.get("k2"));
                }
            }
        });


        Thread threadT3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("k3-->: "+cache.get("k3"));
                }
            }
        });

        threadT1.start();
        threadT2.start();
        threadT3.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 不配置时间失效规则, 测试缓存数据大于maximumSize时, 多线程load数据是否会被阻塞?
     * 结果: 会被阻塞。 t1请求k1未完成时, t2请求k1会被阻塞直到"t1完成请求, k1被加载", t2阻塞释放获得k1的值
     */
    public void test4(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(1).removalListener(new RemovalListener<String, String>() {
            @Override
            public void onRemoval(String key, String value, RemovalCause removalCause) {
                System.out.println("remove key:"+key+" value:"+value);
            }
        }).build(new CacheLoader<String, String>() {
                          @Override
                          public String load(String key) {
                              System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                              try {
                                  Thread.sleep(5000);
                              } catch (InterruptedException e) {
                                  e.printStackTrace();
                              }
                              System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                              return String.valueOf(getCurrentTimeStr());
                          }
                      }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });
        Thread threadT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread2: k1-->: "+cache.get("k1"));
                }
            }
        });


        Thread threadT3 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread3: k3-->: "+cache.get("k3"));
                }
            }
        });

        threadT1.start();
        threadT2.start();
        threadT3.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 配置expireAfterAccess(time1), 测试缓存k1一直被访问时(访问间隔<time1), k1是否会被重新加载?
     * 结果: 不会, 缓存一直生效
     */
    public void test5(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .expireAfterAccess(4, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
            @Override
            public void onRemoval(String key, String value, RemovalCause removalCause) {
                System.out.println("remove key:"+key+" value:"+value);
            }
        }).build(new CacheLoader<String, String>() {
                     @Override
                     public String load(String key) {
                         System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                         try {
                             Thread.sleep(5000);
                         } catch (InterruptedException e) {
                             e.printStackTrace();
                         }
                         System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                         return String.valueOf(getCurrentTimeStr());
                     }
                 }
        );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });

        threadT1.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置expireAfterAccess(time1)+expireAfterWrite(time2), 测试缓存k1一直被访问时(访问间隔<time1), k1什么情况下会被重新加载?
     * 结果: 距离上次访问如果超过time1, 则k1失效; 如果每次访问都不超过time1, 但距离k1上次加载超过time2, 则k1失效, 将被重新加载
     */
    public void test6(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .expireAfterAccess(4, TimeUnit.SECONDS)
                .expireAfterWrite(8, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });

        threadT1.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置expireAfterAccess(time1), 测试缓存k1访问间隔>time1), 多线程加载k1时会被阻塞吗?
     * 结果: k1会失效; 加载k1时会阻塞其他尝试加载k1的线程
     */
    public void test7(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .expireAfterAccess(1, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });

        Thread threadT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread2: k1-->: "+cache.get("k1"));
                }
            }
        });

        threadT1.start();
        threadT2.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置refreshAfterWrite(time1), 测试缓存k1一直被访问时(访问间隔<time1), k1是否会被重新加载?加载时是否阻塞?
     * 结果: 达到time1时自动触发k1异步重新加载。
     * 当k1不存在旧值时，加载k1时会阻塞其他线程。
     * 当k1有值时, 不管触不触发异步加载, 所有访问k1的线程都不会被阻塞
     */
    public void test8(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .refreshAfterWrite(8, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });

        Thread threadT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread2: k1-->: "+cache.get("k1"));
                }
            }
        });

        threadT1.start();
        threadT2.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * 配置refreshAfterWrite(time1) 同时配置 expireAfterWrite(time2), 测试缓存k1一直被访问时(time2>访问间隔>time1), k1是否会被重新加载? 会不会失效?
     * 结果: 满足time2>(time1+加载耗时time3), k1自动被异步加载且k1 k2无阻塞
     * time1如果趋向于无穷小, 几乎每次访问后就会触发异步加载(加载未完成时不会重复触发), 访问量较大时, 接口数据延迟只取决于加载耗时time3
     * 最小延迟为time1+time3, 最大延迟为time2
     */
    public void test9(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        Thread threadT1 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread1: k1-->: "+cache.get("k1"));
                }
            }
        });

        Thread threadT2 = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println("thread2: k1-->: "+cache.get("k1"));
                }
            }
        });

        threadT1.start();
        threadT2.start();

        try {
            threadT1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 配置refreshAfterWrite(time1) 同时配置 expireAfterWrite(time2), 加载间隔time3,
     * k1初始化后, 间隔>time1 再访问一次, time1+time3<time2, 在>time2访问一次, k1是否返回旧值?
     * 结果: k1返回旧值, 加载k1的异步线程为守护进程, 不会阻止JVM的退出
     */
    public void test10(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
    }


    /**
     * 配置refreshAfterWrite(time1) 同时配置 expireAfterWrite(time2), 加载间隔time3,
     * k1初始化后, 间隔>time1 再访问一次, time1+time3<time2, 在>time1+time3+time2, k1是否返回旧值?
     * 结果: k1不返回旧值, 重新阻塞加载返回新值
     */
    public void test11(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(16000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
    }

    /**
     * loadAll包含一个还处于加载中的k, 会被会被阻塞?
     * 结果: k1不返回旧值, 重新阻塞加载返回新值
     */
    public void test12(){
        LoadingCache<String, String> cache = Caffeine.newBuilder().maximumSize(50)
                .refreshAfterWrite(1, TimeUnit.SECONDS)
                .expireAfterWrite(10, TimeUnit.SECONDS)
                .removalListener(new RemovalListener<String, String>() {
                    @Override
                    public void onRemoval(String key, String value, RemovalCause removalCause) {
                        System.out.println("remove key:"+key+" value:"+value);
                    }
                }).build(new CacheLoader<String, String>() {
                             @Override
                             public String load(String key) {
                                 System.out.println("load...key:"+key+" at: "+getCurrentTimeStr());
                                 try {
                                     Thread.sleep(5000);
                                 } catch (InterruptedException e) {
                                     e.printStackTrace();
                                 }
                                 System.out.println("load done...key:"+key+" at: "+getCurrentTimeStr());
                                 return String.valueOf(getCurrentTimeStr());
                             }
                         }
                );

        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        try {
            Thread.sleep(16000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
        System.out.println("k1-->: "+cache.get("k1"));
    }

    public static void main(String[] args) {
        CaffeineDemo1 caffeineDemo1 = new CaffeineDemo1();
        caffeineDemo1.test12();
    }
}
