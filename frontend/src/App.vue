<template>
    <v-app id="inspire">
        <SNSApp style="display: none;" />
        <ReservationNotification />
    </v-app>
</template>

<script>
import SNSApp from './SNSApp.vue'
import ReservationNotification from './components/ReservationNotification.vue'

export default {
    components: {
        SNSApp,
        ReservationNotification
    },
    name: "App",
    data: () => ({
        useComponent: "",
        drawer: true,
        components: [],
        sideBar: true,
        urlPath: null,
    }),
    
    async created() {
        var path = document.location.href.split("#/")
        this.urlPath = path[1];
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
    },
};
</script>

<style>

</style>