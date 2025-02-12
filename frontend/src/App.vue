<template>
    <v-app id="inspire">
        <SNSApp style="display: none;" />
        <div class="notifications-container">
            <transition-group name="slide-notification">
                <div v-for="(notification, index) in activeNotifications" 
                    :key="notification.id" 
                    class="mac-notification"
                    :style="{ top: `${20 + (index * 135)}px` }">
                    <div class="notification-header">
                        <v-icon color="white" small>mdi-bell</v-icon>
                        <span class="ml-2">알림</span>
                        <v-btn
                            icon
                            x-small
                            color="white"
                            @click="removeNotification(notification.id)"
                            class="close-btn"
                        >
                            <v-icon small>mdi-close</v-icon>
                        </v-btn>
                    </div>
                    <div class="notification-content">
                        <div class="notification-title"><h2>{{ notification.title }}</h2></div>
                        <div class="notification-description">
                            {{ truncateText(notification.description) }}
                        </div>
                    </div>
                </div>
            </transition-group>
        </div>
    </v-app>
</template>

<script>
import SNSApp from './SNSApp.vue'
const axios = require('axios').default;

export default {
    components: {
        SNSApp
    },
    name: "App",
    data: () => ({
        useComponent: "",
        drawer: true,
        components: [],
        sideBar: true,
        urlPath: null,
        notifications: [],
        currentDate: null,
        showNotification: false,
        snackbarText: "",
        activeNotifications: [],
        notificationCounter: 0,
        userInfo: null,
    }),
    
    async created() {
        var me = this
        var path = document.location.href.split("#/")
        this.urlPath = path[1];

        me.userInfo = JSON.parse(localStorage.getItem('userInfo'));

        // 초기 알림 목록 로드
        var temp = await axios.get(axios.fixUrl('/notifications'))
        me.notifications = temp.data._embedded.notifications;
        
        // 시간 기반 알림 구독
        const eventSource = new EventSource('/notifications/stream');
        eventSource.addEventListener('time', (event) => {
            const currentTime = event.data;
            me.currentDate = currentTime.substring(0, 16);

            me.notifications.forEach(async function (noti){

                const currentDateTime = new Date(currentTime);
                const dueDateTime = new Date(noti.dueDate);
                
                if (currentDateTime.getFullYear() === dueDateTime.getFullYear() &&
                    currentDateTime.getMonth() === dueDateTime.getMonth() &&
                    currentDateTime.getDate() === dueDateTime.getDate() &&
                    currentDateTime.getHours() === dueDateTime.getHours() &&
                    currentDateTime.getMinutes() === dueDateTime.getMinutes()) {
                    
                    if((!noti.userId && noti.userId == '') || noti.userId == me.userInfo.userId) {
                        var temp = await axios.get(axios.fixUrl('/reservations/' + noti.taskId))
                        if(temp.data) {
                            me.addNotification(temp.data, noti.taskId);
                        }
                    }
                }
            })
        });

        // 실시간 알림 구독
        const notificationSource = new EventSource('/notifications/subscribe');
        notificationSource.addEventListener('notification', (event) => {
            const eventData = JSON.parse(event.data);
            
            if (eventData.type === 'NOTIFICATION_ADDED') {
                const existingNotification = me.notifications.find(n => n.taskId === eventData.notification.taskId);
                if (existingNotification) {
                    existingNotification.dueDate = eventData.notification.dueDate;
                } else {
                    me.notifications.push(eventData.notification);
                }
            } else if (eventData.type === 'NOTIFICATION_DELETED') {
                // 알림이 삭제된 경우
                me.notifications = me.notifications.filter(
                    noti => noti.taskId !== eventData.notificationId
                );
            } else {
                // 일반 실시간 알림인 경우
                if((!eventData.userId && eventData.userId == '') || eventData.userId == me.userInfo.userId) {
                    if (eventData.title && eventData.description) {
                        me.addNotification(eventData, crypto.randomUUID());
                    }
                }
            }
        });
    },

    mounted() {
        var me = this;
        me.components = this.$ManagerLists;
    },

    methods: {
        truncateText(text) {
            if (!text) return '';
            return text.length > 20 ? text.substring(0, 20) + '...' : text;
        },
        openSideBar(){
            this.sideBar = !this.sideBar
        },
        changeUrl() {
            var path = document.location.href.split("#/")
            this.urlPath = path[1];
        },
        goHome() {
            this.urlPath = null;
        },
        removeNotification(id) {
            this.activeNotifications = this.activeNotifications.filter(n => n.id !== id);
        },
        addNotification(noti, taskId) {
            const existingNotification = this.activeNotifications.find(n => n.taskId === taskId);
            if (!existingNotification) {
                this.activeNotifications.push({
                    id: this.notificationCounter++,
                    taskId: taskId,
                    title: noti.title,
                    description: noti.description
                });
            }
        }
    }
};
</script>

<style>
/* 기존 스타일 유지 */
*{
    font-family: "Noto Sans KR", sans-serif !important;
}

.notifications-container {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 999999999;
    pointer-events: none;
    width: auto;
    height: auto;
}

.mac-notification {
    pointer-events: auto;
    position: fixed;
    right: 20px;
    width: 300px;
    background: rgba(50, 50, 50, 0.95);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    margin-bottom: 20px;
    z-index: 999999;
}

.notification-header {
    padding: 12px 15px;
    background: rgba(60, 60, 60, 0.95);
    color: white;
    font-weight: 500;
    display: flex;
    align-items: center;
}

.notification-content {
    padding: 15px;
    color: white;
    font-size: 14px;
}

.close-btn {
    margin-left: auto;
}

.slide-notification-enter-active,
.slide-notification-leave-active {
    transition: all 0.3s ease;
}

.slide-notification-enter-from {
    transform: translateX(100%);
    opacity: 0;
}

.slide-notification-leave-to {
    transform: translateX(100%);
    opacity: 0;
}

.slide-notification-move {
    transition: transform 0.3s ease;
}
</style>