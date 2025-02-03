<template>
    <div>
        <div v-if="editMode">
            <v-row>
                <v-col cols="12">
                    <v-menu
                        ref="dateMenu"
                        v-model="dateMenu"
                        :close-on-content-click="false"
                        transition="scale-transition"
                        offset-y
                        min-width="290px"
                    >
                        <template v-slot:activator="{ on, attrs }">
                            <v-text-field
                                v-model="dateDisplay"
                                :label="label"
                                prepend-icon="mdi-calendar"
                                readonly
                                v-bind="attrs"
                                v-on="on"
                                outlined
                                dense
                            ></v-text-field>
                        </template>
                        <v-date-picker
                            v-model="date"
                            no-title
                            @input="dateMenu = false"
                        ></v-date-picker>
                    </v-menu>
                </v-col>
                <v-col cols="12">
                    <v-menu
                        ref="timeMenu"
                        v-model="timeMenu"
                        :close-on-content-click="false"
                        transition="scale-transition"
                        offset-y
                        min-width="290px"
                    >
                        <template v-slot:activator="{ on, attrs }">
                            <v-text-field
                                v-model="time"
                                label="시간 선택"
                                prepend-icon="mdi-clock-time-four-outline"
                                readonly
                                v-bind="attrs"
                                v-on="on"
                                outlined
                                dense
                            ></v-text-field>
                        </template>
                        <v-time-picker
                            v-model="time"
                            format="24hr"
                            @click:minute="timeMenu = false"
                        ></v-time-picker>
                    </v-menu>
                </v-col>
            </v-row>
        </div>
        <div v-else>
            <v-text-field
                :value="dateTimeDisplay"
                :label="label"
                prepend-icon="mdi-calendar-clock"
                readonly
                outlined
                dense
            ></v-text-field>
        </div>
    </div>
</template>

<script>
export default {
    name: 'Date',
    props: {
        value: String,
        editMode: Boolean,
        label: String
    },
    data: () => ({
        dateMenu: false,
        timeMenu: false,
        date: null,
        time: null,
        dateDisplay: '',
        dateTimeDisplay: ''
    }),
    watch: {
        date() {
            this.updateDateTime();
        },
        time() {
            this.updateDateTime();
        }
    },
    created() {
        if (this.value) {
            // UTC 시간을 로컬 시간으로 변환
            const dateObj = new Date(this.value);
            this.date = dateObj.toISOString().split('T')[0];
            this.time = `${String(dateObj.getHours()).padStart(2, '0')}:${String(dateObj.getMinutes()).padStart(2, '0')}`;
            this.updateDisplays();
        }
    },
    methods: {
        updateDateTime() {
            if (this.date && this.time) {
                // 로컬 시간을 UTC로 변환하여 저장
                const [hours, minutes] = this.time.split(':');
                const localDate = new Date(this.date);
                localDate.setHours(parseInt(hours), parseInt(minutes), 0);
                
                const utcStr = localDate.toISOString();
                this.updateDisplays();
                this.$emit('input', utcStr);
                this.$emit('change');
            }
        },
        updateDisplays() {
            if (this.date) {
                const dateObj = new Date(this.date);
                this.dateDisplay = dateObj.toLocaleDateString('ko-KR', {
                    year: 'numeric',
                    month: 'long',
                    day: 'numeric'
                });
            }
            
            if (this.date && this.time) {
                // 시간은 입력한 그대로 표시
                this.dateTimeDisplay = `${this.dateDisplay} ${this.time}`;
            }
        }
    }
}
</script>