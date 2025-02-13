<template>
    <v-card style="width:450px; height:100%;" outlined>
        <template slot="progress">
            <v-progress-linear
                    color="primary-darker-1"
                    height="10"
                    indeterminate
            ></v-progress-linear>
        </template>

        <v-card-title v-if="value._links">
            Reservation # {{decode(value._links.self.href.split("/")[value._links.self.href.split("/").length - 1])}}
        </v-card-title >
        <v-card-title v-else>
            Reservation
        </v-card-title >        

        <v-card-text style="background-color: white;">
            <String label="target_user_id" v-model="value.userId" :editMode="editMode" :inputUI="''"/>
            <String label="Title" v-model="value.title" :editMode="editMode" :inputUI="''"/>
            <String label="Description" v-model="value.description" :editMode="editMode" :inputUI="''"/>
            <v-switch
                v-if="editMode"
                v-model="isNow"
                :label="isNow ? 'Later' : 'Now'"
            ></v-switch>
            <Date v-if="!isNow" label="DueDate" v-model="value.dueDate" :editMode="editMode" :inputUI="''"/>
        </v-card-text>

        <v-card-actions style="background-color: white;">
            <v-spacer></v-spacer>
            <div v-if="!editMode">
                <v-btn
                    color="primary"
                    text
                    @click="edit"
                >
                    수정
                </v-btn>
                <v-btn
                    color="primary"
                    text
                    @click="remove"
                >
                    삭제
                </v-btn>
            </div>
            <div v-else>
                <v-btn
                    color="primary"
                    text
                    @click="save"
                >
                    등록
                </v-btn>
                <v-btn
                    color="primary"
                    text
                    @click="editMode = false"
                    v-if="editMode && !isNew"
                >
                    취소
                </v-btn>
            </div>
        </v-card-actions>
        <v-card-actions>
            <v-spacer></v-spacer>
        </v-card-actions>

        <v-snackbar
            v-model="snackbar.status"
            :top="true"
            :timeout="snackbar.timeout"
            color="error"
        >
            {{ snackbar.text }}
            <v-btn dark text @click="snackbar.status = false">
                Close
            </v-btn>
        </v-snackbar>
    </v-card>

</template>

<script>
    const axios = require('axios').default;


    export default {
        name: 'ReservationReservation',
        components:{
        },
        props: {
            value: [Object, String, Number, Boolean, Array],
            editMode: Boolean,
            isNew: Boolean,
            offline: Boolean,
        },
        data: () => ({
            snackbar: {
                status: false,
                timeout: 5000,
                text: '',
                color: ''
            },
            isNow: false,
        }),
	async created() {},
    methods: {
        decode(value) {
            return decodeURIComponent(value);
        },
        selectFile(){
            if(this.editMode == false) {
                return false;
            }
            var me = this
            var input = document.createElement("input");
            input.type = "file";
            input.accept = "image/*";
            input.id = "uploadInput";
            
            input.click();
            input.onchange = function (event) {
                var file = event.target.files[0]
                var reader = new FileReader();

                reader.onload = function () {
                    var result = reader.result;
                    me.imageUrl = result;
                    me.value.photo = result;
                    
                };
                reader.readAsDataURL( file );
            };
        },
        edit() {
            this.editMode = true;
        },
        generateUUID() {
            return 'm' + 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = Math.random() * 16 | 0,
                    v = c == 'x' ? r : (r & 0x3 | 0x8);
                return v.toString(16);
            });
        },
        async save(){
            try {
                if(this.isNow) {
                    // 즉시 알림 발송
                    await axios.post(axios.fixUrl('/notifications/broadcast'), {
                        title: this.value.title,
                        userId: this.value.userId,
                        description: this.value.description
                    });
                    this.editMode = false;
                    this.isNow = false;
                    this.$emit('notiNow')
                } else {
                    var temp = null;

                    if(!this.offline) {
                        if(this.isNew) {
                            this.value.taskId = this.generateUUID()
                            temp = await axios.post(axios.fixUrl('/reservations'), this.value);
                        } else {
                            temp = await axios.put(axios.fixUrl(this.value._links.self.href), this.value);
                        }

                        const notificationData = {
                            taskId: this.value.taskId,
                            userId: this.value.userId,
                            dueDate: this.value.dueDate,
                            title: this.value.title,
                            description: this.value.description
                        };
                        
                        await axios.post(axios.fixUrl('/notifications/reminder'), notificationData);
                    }

                    if(this.value!=null) {
                        for(var k in temp.data) {
                            this.value[k] = temp.data[k];
                        }
                    } else {
                        this.value = temp.data;
                    }

                    this.editMode = false;
                    this.$emit('input', this.value);

                    if (this.isNew) {
                        this.$emit('add', this.value);
                    } else {
                        this.$emit('edit', this.value);
                    }

                    this.snackbar.text = '성공적으로 저장되었습니다.';
                    this.snackbar.color = 'success';
                    this.snackbar.status = true;

                    setTimeout(() => {
                        location.reload();
                    }, 1000);
                }

            } catch(e) {
                this.snackbar.color = 'error';
                if(e.response && e.response.data.message) {
                    this.snackbar.text = e.response.data.message;
                } else {
                    this.snackbar.text = '저장 중 오류가 발생했습니다.';
                }
                this.snackbar.status = true;
            }
        },
        async remove(){
            try {
                if (!this.offline) {
                    await axios.delete(axios.fixUrl('/reservations/' + this.value.taskId));
                    
                    await axios.post(axios.fixUrl('/notifications/broadcast'), {
                        type: 'NOTIFICATION_DELETED',
                        taskId: this.value.taskId
                    }); 
                }

                this.editMode = false;

                this.$emit('input', this.value);
                this.$emit('delete', this.value);

            } catch(e) {
                this.snackbar.status = true
                if(e.response && e.response.data.message) {
                    this.snackbar.text = e.response.data.message
                } else {
                    this.snackbar.text = e
                }
            }
        },
        change(){
            this.$emit('input', this.value);
        },
    },
}
</script>

