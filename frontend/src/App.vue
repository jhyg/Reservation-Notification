<template>
    <v-app id="inspire">
        <SNSApp style="display: none;" />
        <!-- <div class="notifications-container" :style="isSecureContext && notificationPermission ? 'display: none;' : ''"> -->
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
        reservations: [],
        currentDate: null,
        showNotification: false,
        snackbarText: "",
        activeNotifications: [],
        notificationCounter: 0,
        userInfo: null,
        notificationPermission: false,
        isSecureContext: false,
    }),
    
    async created() {
        var me = this
        var path = document.location.href.split("#/")
        this.urlPath = path[1];

        me.userInfo = JSON.parse(localStorage.getItem('userInfo'));

        // 초기 알림 목록 로드
        var temp = await axios.get(axios.fixUrl('/reservations'))
        me.reservations = temp.data;
        
        // 알림 권한 요청
        this.isSecureContext = window.isSecureContext && "Notification" in window;
        
        // 보안 컨텍스트에서만 알림 권한 요청
        if (this.isSecureContext) {
            try {
                const permission = await Notification.requestPermission();
                this.notificationPermission = permission === "granted";
            } catch (error) {
                console.warn('알림 권한 요청 실패:', error);
                this.notificationPermission = false;
            }
        }

        // 시간 기반 알림 구독
        const eventSource = new EventSource('/notifications/stream');
        eventSource.addEventListener('time', (event) => {
            const currentTime = event.data;
            me.currentDate = currentTime.substring(0, 16);

            me.reservations.forEach(async function (reservation){
                const currentDateTime = new Date(currentTime);
                const dueDateTime = new Date(reservation.dueDate);
                const tenMinutesBeforeDue = new Date(dueDateTime.getTime() - 10 * 60000);
                
                if (currentDateTime.getFullYear() === tenMinutesBeforeDue.getFullYear() &&
                    currentDateTime.getMonth() === tenMinutesBeforeDue.getMonth() &&
                    currentDateTime.getDate() === tenMinutesBeforeDue.getDate() &&
                    currentDateTime.getHours() === tenMinutesBeforeDue.getHours() &&
                    currentDateTime.getMinutes() === tenMinutesBeforeDue.getMinutes()) {
                    
                    if((!reservation.userId && reservation.userId == '') || reservation.userId == me.userInfo.userId) {
                        me.addNotification({
                            ...reservation,
                            taskId: `${reservation.taskId}_10min`,
                            displayDescription: `[10분 후 시작] ${reservation.description}`
                        });
                    }
                } else if (currentDateTime.getFullYear() === dueDateTime.getFullYear() &&
                    currentDateTime.getMonth() === dueDateTime.getMonth() &&
                    currentDateTime.getDate() === dueDateTime.getDate() &&
                    currentDateTime.getHours() === dueDateTime.getHours() &&
                    currentDateTime.getMinutes() === dueDateTime.getMinutes()) {
                    
                    if((!reservation.userId && reservation.userId == '') || reservation.userId == me.userInfo.userId) {
                        me.addNotification({
                            ...reservation,
                            taskId: `${reservation.taskId}_start`,
                            displayDescription: `[일정 시작] ${reservation.description}`
                        });
                    }
                } else if (dueDateTime < currentDateTime) {
                    await axios.delete(axios.fixUrl('/reservations/' + reservation.taskId));
                    
                    await axios.post(axios.fixUrl('/notifications/broadcast'), {
                        type: 'NOTIFICATION_DELETED',
                        taskId: reservation.taskId
                    });
                }
            })
        });

        // 실시간 알림 구독
        const notificationSource = new EventSource('/notifications/subscribe');
        notificationSource.addEventListener('notification', (event) => {
            const eventData = JSON.parse(event.data);
            
            if (eventData.type === 'NOTIFICATION_ADDED') {
                const existingNotification = me.reservations.find(n => n.taskId === eventData.notification.taskId);
                if (existingNotification) {
                    existingNotification.dueDate = eventData.notification.dueDate;
                } else {
                    me.reservations.push(eventData.notification);
                }
            } else if (eventData.type === 'NOTIFICATION_DELETED') {
                // 알림이 삭제된 경우
                me.reservations = me.reservations.filter(
                    noti => noti.taskId !== eventData.taskId
                );
            } else {
                // 일반 실시간 알림인 경우
                if((!eventData.userId && eventData.userId == '') || eventData.userId == me.userInfo.userId) {
                    if (eventData.title && eventData.description) {
                        me.addNotification(eventData);
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
        generateUUID() {
            return 'm' + 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0,
                    v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
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
        addNotification(reservation) {
            if(!reservation.taskId) {
                reservation.taskId = this.generateUUID();
            }
            const existingNotification = this.activeNotifications.find(n => n.taskId === reservation.taskId);
            if (!existingNotification) {
                this.activeNotifications.push({
                    id: this.notificationCounter++,
                    taskId: reservation.taskId,
                    title: reservation.title,
                    description: reservation.displayDescription || reservation.description
                });

                // 시스템 알림 표시
                if (this.isSecureContext && this.notificationPermission) {
                    try {
                        new Notification(reservation.title, {
                            body: reservation.displayDescription || reservation.description,
                            silent: false,
                            requireInteraction: true
                        });
                    } catch (error) {
                        console.warn('시스템 알림 표시 실패:', error);
                    }
                }
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

me.reservations-container {
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