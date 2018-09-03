<template>
  <transition
    enter-active-class="animated quick fadeInRight"
    leave-active-class="animated quick fadeOutRight"
  >
    <div v-if="show" :class="[container,status, 'notify-me']" :style="{ width: width }">
      <slot name="content"></slot>
      <!--<button v-if="close === 'bulma'" class="delete" @click="hideMe"></button>-->
      <!--<button v-else-if="close === 'bootstrap'" type="button" class="close" aria-label="Close" @click="hideMe">-->
      <!--<span aria-hidden="true">&times;</span>-->
      <!--</button>-->
      <!--<i v-else :class="close + ' material-icons'" @click="hideMe">clear</i>-->
    </div>
  </transition>
</template>
<script>
  export default {
    props: {
      permanent: {
        default: false
      },
      close: '',
      content: {},
      container: {
        type: String,
        default: 'alert'
      },
      status: {
        type: String,
        default: 'alert-success'
      },
      width: {
        type: String,
        default: '350px'
      },
      timeout: {
        type: Number,
        default: 3000
      }

    },
    data() {
      return {
        show: true
      }
    },
    methods: {
      hideMe() {
        this.$emit('hide');
      }
    },
    created() {
      if (!this.permanent) {
        setTimeout(() => {
          this.hideMe();
        }, this.timeout)
      }

    }

  }
</script>
<style scoped>
  .notify-me {
    display: flex;
    align-items: center;
    justify-content: space-between;
    position: relative;
    bottom: 2rem;
    right: 2rem;
    z-index: 9999;
    margin-bottom: 1.5rem;
  }

  .notify-me i {
    cursor: pointer;
    align-self: flex-start;
  }

  .notify-me:first-child {
    margin-bottom: 0;
  }

  .notification {
    background-color: #f5f5f5;
    border-radius: 3px;
    padding: 1.25rem 2.5rem 1.25rem 1.5rem;
    position: relative;
  }

  .notification.is-danger {
    background-color: #ff3860;
    color: #fff;
  }

  .notification.is-info {
    background-color: #3273dc;
    color: #fff;
  }

  .notification.is-success {
    background-color: #23d160;
    color: #fff;
  }

  .notification.is-warning {
    background-color: #ffdd57;
    color: #fff;
  }


  .animated {
    -webkit-animation-duration: 1s;
    -webkit-animation-fill-mode: both;
    animation-duration: 1s;
    animation-fill-mode: both
  }
  @-webkit-keyframes fadeInRight {
    0% {
      -webkit-transform: translate3d(100%, 0, 0);
      opacity: 0;
      transform: translate3d(100%, 0, 0)
    }
    to {
      -webkit-transform: translateZ(0);
      opacity: 1;
      transform: translateZ(0)
    }
  }

  @keyframes fadeInRight {
    0% {
      -webkit-transform: translate3d(100%, 0, 0);
      opacity: 0;
      transform: translate3d(100%, 0, 0)
    }
    to {
      -webkit-transform: translateZ(0);
      opacity: 1;
      transform: translateZ(0)
    }
  }

  .fadeInRight {
    -webkit-animation-name: fadeInRight;
    animation-name: fadeInRight
  }


  @-webkit-keyframes fadeOutRight {
    0% {
      opacity: 1
    }
    to {
      -webkit-transform: translate3d(100%, 0, 0);
      opacity: 0;
      transform: translate3d(100%, 0, 0)
    }
  }

  @keyframes fadeOutRight {
    0% {
      opacity: 1
    }
    to {
      -webkit-transform: translate3d(100%, 0, 0);
      opacity: 0;
      transform: translate3d(100%, 0, 0)
    }
  }

  .fadeOutRight {
    -webkit-animation-name: fadeOutRight;
    animation-name: fadeOutRight
  }

  .quick {
    transition: all 1.5s
  }
</style>
