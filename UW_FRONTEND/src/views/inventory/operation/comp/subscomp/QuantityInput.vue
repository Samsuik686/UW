<template>
    <el-input type="text"
              :class="{highLight:isHighLight}"
              @click.native="setMaterialIdActive"
              ref="myInput"
              @blur="cancelFocus"
              v-model.trim="inputVal"></el-input>
</template>

<script>
    import Bus from './../../../../../utils/bus'
    import {mapActions,mapGetters} from 'vuex'
    import {inventoryMaterialUrl} from "../../../../../plugins/globalUrl";
    import {axiosPost} from "../../../../../utils/fetchData";
    import {errHandler} from "../../../../../utils/errorHandler";
    export default {
        name: "QuantityInput",
        props:{
            row:Object,
            activeMaterialId:String,
            activeQuantity:Number,
            boxId:Number,
            taskId:Number,
            thisWindow:String
        },
        computed:{
            ...mapGetters(['isScanner','printMaterialIdArr','disabledMaterialId'])
        },
        data(){
            return{
                inputVal:'',
                isHighLight:false
            }
        },
        mounted(){
            this.inputVal = this.row.actualNum;
        },
        watch:{
            activeMaterialId:function(val){
                if(val === this.row.materialId){
                    this.isHighLight = true;
                    if(this.activeQuantity === -1){
                        this.inputVal = this.row.actualNum;
                    }else{
                        this.inputVal = this.activeQuantity;
                        Bus.$emit('setActiveQuantity',-1);
                    }
                    if(this.isScanner === false){
                        this.setFocus();
                    }else{
                        this.checkQuantity();
                    }
                }else{
                    this.isHighLight = false;
                }
            },
            thisWindow:function(val){
                if(val !== ''){
                    this.isHighLight = false;
                    this.inputVal = '';
                    let that = this;
                    setTimeout(function(){
                        that.inputVal = that.row.actualNum;
                    },1000)
                }
            }
        },
        methods:{
            ...mapActions(['setIsFocus','setIsScanner','setDisabledMaterialId','setPrintMaterialIdArr']),
            setFocus:function(){
                //使外部输入框失焦
                this.setIsFocus(true);
                this.setDisabledMaterialId(this.row.materialId);
                this.$refs.myInput.focus();
            },
            cancelFocus:function(){
                //使外部输入框聚焦
                this.setIsFocus(false);
                this.setDisabledMaterialId('');
                if(this.inputVal === '' || this.inputVal === null){
                    this.inputVal = this.row.actualNum;
                    Bus.$emit('refreshInventory',true);
                    return;
                }
                if(Number(this.inputVal) === Number(this.row.actualNum) && this.row.actualNum !== null){
                    Bus.$emit('refreshInventory',true);
                    return;
                }
                this.checkQuantity();
            },
            setMaterialIdActive:function(){
                this.setIsScanner(false);
                Bus.$emit('setActiveMaterialId',this.row.materialId);
            },
            checkQuantity:function(){
                if(this.inputVal === '' || this.inputVal === null){
                    return;
                }
                if(!this.isNumber(this.inputVal)){
                    this.$alertWarning('盘点数量必须为非负整数');
                    this.inputVal =  this.row.actualNum;
                    return;
                }
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: inventoryMaterialUrl,
                        data: {
                            materialId: this.row.materialId,
                            boxId: this.boxId,
                            taskId: this.taskId,
                            acturalNum: this.inputVal
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            Bus.$emit('refreshInventory',true);
                            this.cancelPrintArr(this.row.materialId);
                            this.$emit('setPosition',response.data.data.col,response.data.data.row);
                        } else {
                            errHandler(response.data);
                        }
                    }).catch(err => {
                        this.$alertError('连接超时，请刷新重试');
                        console.log(err);
                    }).finally(() => {
                        this.isPending =false;
                    })
                }
            },
            isNumber: function (num) {
                let val = num;
                let reg = /^\+?(0|[1-9][0-9]*)$/;
                if (val !== "") {
                    return reg.test(val);
                }
            },
            cancelPrintArr:function (materialId){
                let arr = [];
                for(let i=0;i<this.printMaterialIdArr.length;i++){
                    if(this.printMaterialIdArr[i] !== materialId){
                        arr.push(this.printMaterialIdArr[i]);
                    }
                }
                this.setPrintMaterialIdArr(arr);
            }
        }
    }
</script>

<style scoped>
</style>