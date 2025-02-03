<template>
    <v-app id="inspire">
        <SNSApp />
        <div class="notifications-container">
            <transition-group name="slide-notification">
                <div v-for="(notification, index) in activeNotifications" 
                     :key="notification.id" 
                     class="mac-notification"
                     :style="{ top: `${20 + (index * 100)}px` }">
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
                        {{ notification.text }}
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
        // 맥 스타일 알림 관련 데이터 추가
        showNotification: false,
        snackbarText: "",
        activeNotifications: [],
        notificationCounter: 0,
    }),
    
    async created() {
        var me = this
        var path = document.location.href.split("#/")
        this.urlPath = path[1];

        var temp = await axios.get(axios.fixUrl('/notifications'))
        me.notifications = temp.data._embedded.notifications;
        
        // SSE 연결 설정
        const eventSource = new EventSource('/notifications/stream');
        eventSource.addEventListener('time', (event) => {
            const currentTime = event.data;
            console.log('Current time:', currentTime);

            me.currentDate = currentTime.substring(0, 16);

            me.notifications.forEach(async function (noti){
                const dueDateObj = noti.dueDate.substring(0, 16);
                if (me.currentDate === dueDateObj) {
                    var temp = await axios.get(axios.fixUrl('/reservations/' + noti.taskId))
                    if(temp.data) {
                        me.addNotification(temp.data.title, noti.taskId);
                    }
                }
            })
        });
    },

    mounted() {
        var me = this;
        me.components = this.$ManagerLists;
    },

    methods: {
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
        addNotification(text, taskId) {
            const exists = this.activeNotifications.some(n => n.taskId === taskId);
            if (!exists) {
                this.activeNotifications.push({
                    id: this.notificationCounter++,
                    text,
                    taskId
                });
            }
        }
    }
};
</script>
<style>
*{
    font-family: "Noto Sans KR", sans-serif !important;
}

.notifications-container {
    position: fixed;
    top: 0;
    right: 0;
    z-index: 9999;
    pointer-events: none; /* 컨테이너는 클릭 이벤트를 통과시킴 */
}

.mac-notification {
    pointer-events: auto; /* 개별 알림은 클릭 가능하게 */
    position: fixed;
    right: 20px;
    width: 300px;
    background: rgba(50, 50, 50, 0.95);
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
    overflow: hidden;
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

/* 애니메이션 수정 */
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

/* 위치 변경 애니메이션 */
.slide-notification-move {
    transition: transform 0.3s ease;
}
</style>
