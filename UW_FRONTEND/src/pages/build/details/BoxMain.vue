<template>
  <div class="box">
    <div class="left">
      <div class="header">
        <button class="btn btn-primary mr-3" @click="addCellWidth">新增规格</button>
        <button class="btn btn-primary mr-3" @click="isTypeAdding = !isTypeAdding">添加料盒类型</button>
        <button class="btn btn-primary" @click="build">入仓</button>
      </div>
      <div class="body">
        <ul>
          <li v-for="(item,index) in parameters">
            <span class="name">规格 {{item.boxType}}</span>
            <div>
              <span title="编辑" class="icon-edit" style="margin-right:5px;" @click="editCellWidth(item,index)"><icon
                name="edit" scale="1.8"></icon></span>
              <span title="删除" class="icon-delete" @click="deleteItem(index)"><icon name="cancel" scale="1.8"></icon></span>
            </div>
          </li>
        </ul>
      </div>
    </div>
    <div class="right">
      <div v-if="isAdding" id="add-window">
        <add-cell-width @getAddData='getAddData'/>
      </div>
      <div v-if="isEditing" id="edit-window">
        <edit-cell-width @getEditData='getEditData' :editData="editData"/>
      </div>
    </div>
    <transition name="fade">
      <div v-if="isTypeAdding" id="add-type-window">
        <add-box-type/>
      </div>
    </transition>
  </div>
</template>

<script>
  import AddBoxType from './comp/AddBoxType'
  import EditCellWidth from './comp/EditCellWidth'
  import AddCellWidth from './comp/AddCellWidth'
  import eventBus from "../../../utils/eventBus";
  import {buildUrl} from "../../../config/globalUrl";
  import {axiosPost} from "../../../utils/fetchData";
  import {errHandler} from "../../../utils/errorHandler";

  export default {
    name: "BoxMain",
    data() {
      return {
        parameters: [],
        editData: {},
        isTypeAdding: false,
        isAdding: false,
        isEditing: false,
        isPending: false
      }
    },
    components: {
      AddBoxType,
      AddCellWidth,
      EditCellWidth
    },
    mounted() {
      eventBus.$on('closeAddPanel', () => {
        this.isAdding = false;
      });
      eventBus.$on('closeEditPanel', () => {
        this.isEditing = false;
      });
      eventBus.$on('closeAddTypePanel', () => {
        this.isTypeAdding = false;
      });
    },
    methods: {
      addCellWidth: function () {
        this.isEditing = false;
        this.isAdding = true;
      },
      editCellWidth: function (item, index) {
        this.isAdding = false;
        this.isEditing = false;
        this.$nextTick(function () {
          this.isEditing = true;
          this.editData = item;
          this.editData["index"] = index;
        })
      },
      deleteItem: function (index) {
        this.isAdding = false;
        this.isEditing = false;
        this.parameters.splice(index, 1);
      },
      getAddData: function (item) {
        if(this.judge(item)){
          this.isAdding = false;
          this.parameters.push(item);
        }
      },
      getEditData: function (item, index) {
        if(this.judge(item,index)){
          this.isEditing = false;
          this.parameters.splice(index, 1, item);
        }
      },
      build: function () {
        if(this.parameters.length <= 0){
          this.$alertWarning("内容不能为空");
          return;
        }
        if (!this.isPending) {
          this.isPending = true;
          let options = {
            url: buildUrl,
            data:{
              parameters:JSON.stringify(this.parameters)
            }
          };
          axiosPost(options).then(response => {
            this.isPending = false;
            if (response.data.result === 200) {
              this.$alertSuccess('建仓成功');
              this.$router.replace('/');
            } else if (response.data.result >= 412 && response.data.result < 500) {
              this.$alertWarning(response.data.data)
            } else {
              errHandler(response.data)
            }
            this.isPending = false;
          })
        }
      },
      judge:function(item,index){
        for(let i=0;i<this.parameters.length;i++){
          if(i !== index){
            let obj = this.parameters[i];
            if(item.boxType === obj.boxType){
              this.$alertWarning("不能添加相同规格的料盒");
              return false;
            }
            if(item.srcX === obj.srcX && item.srcY === obj.srcY && item.srcZ === item.srcZ){
              this.$alertWarning("不同规格的料盒不能有相同的起始位置");
              return false;
            }
            if(JSON.stringify(item.dst) === JSON.stringify(obj.dst)){
              this.$alertWarning("不同规格的料盒不能有相同的目标位置");
              return false;
            }
          }
        }
        return true;
      }
    }
  }
</script>

<style scoped>
  .box {
    box-sizing: border-box;
    display: flex;
    padding: 40px 60px;
    width: 100%;
    min-width: 940px;
    height: 100%;
    min-height: 600px;
    background: #fff;
    border: 1px solid #eeeeee;
    border-radius: 8px;
  }

  .left {
    width: 25%;
    min-width: 380px;
    max-height:100%;
    padding-right: 40px;
    border-right: 1px solid #eee;
    overflow-y: auto;
  }

  .right {
    width: 75%;
    padding-left:20px;
    min-width: 550px;
    max-height:100%;
    overflow-y: auto;
    overflow-x:hidden;
  }

  .header {
    margin: 20px 0;
  }

  ul, li {
    list-style: none;
    margin: 0;
    padding: 0;
  }

  li {
    display: flex;
    justify-content: space-between;
    height: 50px;
    line-height: 50px;
    cursor: pointer;
  }

  #add-window {
    z-index: 100;
  }

  #add-type-window {
    z-index: 100;
  }

  .fade-enter-active, .fade-leave-active {
    transition: opacity .5s;
  }

  .fade-enter, .fade-leave-to {
    opacity: 0;
  }
</style>
