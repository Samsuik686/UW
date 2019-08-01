<template>
    <div class="build">
        <el-form class="build-form" :model="buildInfo">
            <el-form-item label="供应商">
                <el-select v-model.trim="buildInfo.supplierId" placeholder="供应商" value="">
                    <el-option  v-for="item in suppliers" :label="item.name" :value='item.id' :key="item.id"></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="区域">
                <el-select v-model.trim="buildInfo.parameters.area" placeholder="区域" value="">
                    <el-option label="A" value='A'></el-option>
                    <el-option label="B" value='B'></el-option>
                    <el-option label="C" value='C'></el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="料盒类型">
                <el-radio-group v-model="isStandard">
                    <el-radio label='1'>标准</el-radio>
                    <el-radio label='0'>不标准</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="起始位置">
                <el-input v-model.trim.number="buildInfo.parameters.srcX" placeholder="srcX" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.srcY" placeholder="srcY" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.srcZ" placeholder="srcZ" type="number"></el-input>
            </el-form-item>
            <el-form-item label="目标位置 - 起始坐标">
                <el-input v-model.trim.number="buildInfo.parameters.startX" placeholder="startX" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.startY" placeholder="startY" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.startZ" placeholder="startZ" type="number"></el-input>
            </el-form-item>
            <el-form-item label="目标位置 - 终止坐标">
                <el-input v-model.trim.number="buildInfo.parameters.startX" placeholder="endX" disabled type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.endY" placeholder="endY" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.endZ" placeholder="endZ" type="number"></el-input>
            </el-form-item>
            <el-form-item label="边界坐标">
                <el-input v-model.trim.number="buildInfo.parameters.limitYL" placeholder="limitYL" type="number"></el-input>
                <el-input v-model.trim.number="buildInfo.parameters.limitYR" placeholder="limitYR" type="number"></el-input>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-coke-build" @click="build">建仓</el-button>
                <el-button type="info" icon="el-icon-close" @click="initForm">清除条件</el-button>
            </el-form-item>
        </el-form>
    </div>
</template>

<script>
    import {buildUrl, supplierSelectUrl} from "../../plugins/globalUrl";
    import {axiosPost} from "../../utils/fetchData";
    import {errHandler} from "../../utils/errorHandler";

    export default {
        name: "Build",
        data(){
            return{
                buildInfo:{
                    parameters: {
                        area:'',
                        srcX: '',
                        srcY: '',
                        srcZ: '',
                        startX: '',
                        startY: '',
                        startZ: '',
                        endX:'',
                        endY: '',
                        endZ: '',
                        limitYL: '',
                        limitYR: ''
                    },
                    supplierId:'',
                    isStandard:'',
                },
                isStandard:'',
                suppliers:[],
                isPending:false
            }
        },
        created(){
            this.selectSupplier();
        },
        methods:{
            initForm:function(){
                this.buildInfo.isStandard = '';
                this.buildInfo.supplierId = '';
                this.buildInfo.parameters.area = '';
                this.buildInfo.parameters.limitYL = '';
                this.buildInfo.parameters.limitYR = '';
                this.buildInfo.parameters.srcX = '';
                this.buildInfo.parameters.srcY = '';
                this.buildInfo.parameters.srcZ = '';
                this.buildInfo.parameters.startX = '';
                this.buildInfo.parameters.startY = '';
                this.buildInfo.parameters.startZ = '';
                this.buildInfo.parameters.endX = '';
                this.buildInfo.parameters.endY = '';
                this.buildInfo.parameters.endZ = '';
                this.isStandard = '';
            },
            selectSupplier:function(){
                let options = {
                    url: supplierSelectUrl,
                    data: {}
                };
                axiosPost(options).then(res => {
                    if(res.data.result === 200){
                        let data = res.data.data.list;
                        data.map((item) => {
                            if(item.enabled === true){
                                this.suppliers.push(item);
                            }
                        });
                    }else{
                        errHandler(res.data)
                    }
                }).catch(err => {
                    console.log(err);
                    this.$alertError('连接超时，请刷新重试');
                })
            },
            build:function(){
                this.buildInfo.parameters.endX = this.buildInfo.parameters.startX;
                if (this.buildInfo.supplierId === '') {
                    this.$alertWarning('supplierId不能为空');
                    return;
                }
                if (this.isStandard === '') {
                    this.$alertWarning('isStandard不能为空');
                    return;
                }
                for (let item in this.buildInfo.parameters) {
                    if (this.buildInfo.parameters[item] === '') {
                        this.$alertWarning(item + '不能为空');
                        return;
                    }
                }
                this.buildInfo.isStandard = this.isStandard === '1';
                if (!this.isPending) {
                    this.isPending = true;
                    let options = {
                        url: buildUrl,
                        data: {
                            parameters: JSON.stringify(this.buildInfo)
                        }
                    };
                    axiosPost(options).then(response => {
                        if (response.data.result === 200) {
                            this.$alertSuccess('该供应商的料盒入仓成功');
                        } else {
                            errHandler(response.data)
                        }
                    }).catch(err => {
                        console.log(err);
                        this.$alertError('连接超时，请刷新重试');
                    }).finally(() => {
                        this.isPending = false;
                    })
                }
            }
        }
    }
</script>

<style scoped lang="scss">
    .build{
        box-sizing: border-box;
        width:100%;
        height:100%;
        padding:30px 30px;
        background:#fff;
        border-radius:6px;
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
        overflow-y: auto;
        .build-form{
            .el-input{
                width:200px;
                margin-right:10px;
            }
        }
    }
</style>